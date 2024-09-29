package net.wirelabs.jmaps.map.cache;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.utils.ImageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.ThreadUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static net.wirelabs.jmaps.TestUtils.cacheCheckExistenceAndExpiration;
import static net.wirelabs.jmaps.TestUtils.cacheAssertSameData;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DBCacheTest {

    private static BufferedImage testImg1;
    private static BufferedImage testImg2;

    private static final String TEST_TILE_URL_1 = "http://a.b.c.pl/a/b/xxx.png";
    private static final String TEST_TILE_URL_2 = "http://a.b.c.pl/a/b/yyy.png";
    private static final String NONEXISTENT_TILE_URL = "nonexistent";
    private static final Path TEST_TILECACHE_DB = Paths.get("target/test-db");
    private static final Duration TEST_CACHE_TIMEOUT = Duration.ofHours(2);

    @BeforeEach
    void beforeEach() throws IOException {
            // before each test shutdown any existing testcache instance, and delete its directory
            try {
                String path = String.format("jdbc:derby:%s;shutdown=true", TEST_TILECACHE_DB);
                DriverManager.getConnection(path);
            } catch (SQLException ex) {
                // derby generates exception on sucessful close/shutdown (really ;))
                // the codes for ok shutdown are xj015 and 08006 (and xj004 in this case (no database open) - otherwise, it's a real exception
                if (!ex.getSQLState().equals("XJ015") && !ex.getSQLState().equals("08006") && !ex.getSQLState().equals("XJ004")) {
                    throw new IOException("Could not shutdown derby");
                }
                FileUtils.deleteDirectory(TEST_TILECACHE_DB.toFile());
                assertThat(TEST_TILECACHE_DB.toFile()).doesNotExist();
            }
    }

    @BeforeAll
    static void beforeAll() throws IOException {
        File imgFile = new File("src/test/resources/tiles/tile.png");
        File imgFileOther = new File("src/test/resources/tiles/tile-other.png");
        testImg1 = ImageIO.read(imgFile);
        testImg2 = ImageIO.read(imgFileOther);
    }

    @Test
    void testDefaultCacheInit() throws SQLException {
        DBCache cache = new DBCache();
        assertThat(cache.getCacheTimeout()).isEqualTo(Defaults.DEFAULT_CACHE_TIMEOUT);
        assertThat(cache.getBaseDir()).isEqualTo(Defaults.DEFAULT_TILE_CACHE_DB);
        assertThat(tileCacheTableExists(cache)).isTrue();
    }

    @Test
    void testCustomCacheInit() throws SQLException {
        DBCache cache = new DBCache(TEST_TILECACHE_DB, TEST_CACHE_TIMEOUT);
        assertThat(cache.getCacheTimeout()).isEqualTo(TEST_CACHE_TIMEOUT);
        assertThat(cache.getBaseDir()).isEqualTo(TEST_TILECACHE_DB);
        assertThat(tileCacheTableExists(cache)).isTrue();
    }

    @Test
    void putAndGetTest() throws SQLException {
        DBCache cache = new DBCache(TEST_TILECACHE_DB, TEST_CACHE_TIMEOUT);
        // put
        cache.put(TEST_TILE_URL_1, testImg1);
        cache.put(TEST_TILE_URL_2, testImg2);

        // assert both are there it's there and has the same data (written ok)
        cacheCheckExistenceAndExpiration(cache, TEST_TILE_URL_1, true, false);
        cacheCheckExistenceAndExpiration(cache, TEST_TILE_URL_2, true, false);

        cacheAssertSameData(cache, TEST_TILE_URL_1, testImg1);
        cacheAssertSameData(cache, TEST_TILE_URL_2, testImg2);

        // assert different timestamps
        assertThat(getTimestamp(cache,TEST_TILE_URL_1)).isNotEqualTo(getTimestamp(cache,TEST_TILE_URL_2));

        // now get nonexisting key - should return nonexistent and not-expired  (cache-api)
        cacheCheckExistenceAndExpiration(cache, NONEXISTENT_TILE_URL, false, false);
    }

    @Test
    void shouldInsertOrUpdate() throws SQLException {
        List<BufferedImage> images = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>();

        DBCache cache = new DBCache(TEST_TILECACHE_DB, Defaults.DEFAULT_CACHE_TIMEOUT);


        // put entry 2 times in a row, with different image - should result in two different timestamps and different images
        cache.put(TEST_TILE_URL_1, testImg1);
        images.add(cache.get(TEST_TILE_URL_1));
        timestamps.add(getTimestamp(cache,TEST_TILE_URL_1));

        cache.put(TEST_TILE_URL_1, testImg2);
        images.add(cache.get(TEST_TILE_URL_1));
        timestamps.add(getTimestamp(cache,TEST_TILE_URL_1));

        // assert images and timestamps differ
        assertThat(ImageUtils.imagesEqual(images.get(0), images.get(1))).isFalse();
        assertThat(timestamps.get(0)).isNotEqualTo(timestamps.get(1));
    }

    @Test
    void shouldCheckIfTileValidWhenCacheNonZeroAndNotCheckWhenZero() throws InterruptedException {
        // cache with default timeout
        DBCache cache = new DBCache(TEST_TILECACHE_DB, Defaults.DEFAULT_CACHE_TIMEOUT);
        // put some tile in - check if it is there and not expired
        cache.put(TEST_TILE_URL_1, testImg1);
        cacheCheckExistenceAndExpiration(cache, TEST_TILE_URL_1, true, false);

        // now disable cache validity check
        cache.setCacheTimeout(Duration.ZERO);
        // it should always be valid (expired = false)
        assertThat(cache.keyExpired(TEST_TILE_URL_1)).isFalse();

        // now enable validity check with 2 seconds time
        cache.setCacheTimeout(Duration.ofSeconds(2));
        // after a second it should be stil there and valid (expired=false)
        ThreadUtils.sleep(Duration.ofSeconds(1));
        cacheCheckExistenceAndExpiration(cache, TEST_TILE_URL_1, true, false);

        // but now 3 sec  will pass. it should expire
        ThreadUtils.sleep(Duration.ofSeconds(2));
        cacheCheckExistenceAndExpiration(cache, TEST_TILE_URL_1, true, true);

    }

    @Test
    void shouldNotReportKeyExpiredOnNonexistent() {
        DBCache cache = new DBCache(TEST_TILECACHE_DB, Defaults.DEFAULT_CACHE_TIMEOUT);
        assertThat(cache.keyExpired("NONEXISTENT")).isFalse();
    }

    private long getTimestamp(DBCache cache,String key) throws SQLException {
        String timeStampQuery = String.format("select TIMESTAMP from TILECACHE where TILEURL='%s'", key);
        try (Statement stmt = cache.getConnection().createStatement(); ResultSet rs = stmt.executeQuery(timeStampQuery)) {
            assertThat(rs.next()).isTrue();
            return rs.getLong(1);
        }
    }

    private boolean tileCacheTableExists(DBCache cache) throws SQLException {
        DatabaseMetaData dbmd = cache.getConnection().getMetaData();
        try (ResultSet rs = dbmd.getTables(null, null, "TILECACHE", null)) {
            return rs.next();
        }
    }
}