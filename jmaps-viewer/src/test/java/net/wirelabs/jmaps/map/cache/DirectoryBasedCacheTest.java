package net.wirelabs.jmaps.map.cache;

import net.wirelabs.jmaps.TestUtils;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.utils.BaseTest;
import net.wirelabs.jmaps.map.utils.ImageUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.ThreadUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static net.wirelabs.jmaps.TestUtils.cacheCheckExistenceAndExpiration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created 5/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class DirectoryBasedCacheTest extends BaseTest {

    private static final Path TEST_CACHE_DIR = Paths.get("target/testcache/tile-cache");
    private static final Duration SHORT_TIMEOUT_FOR_VALIDITY_TESTS = Duration.ofSeconds(2);

    private static final File TEST_IMAGE_FILE = new File("src/test/resources/tiles/tile.png");
    private static final File TEST_IMAGE_OTHER_FILE = new File("src/test/resources/tiles/tile-other.png");

    private static final String XYZ_URL_WITHOUT_QUERY = "http://tile.openstreetmap.org/2/4/5.jpg";
    private static final String XYZ_URL_WITH_QUERY = "http://tile.openstreetmap.org/3/5/6.jpg?apiKey=120931092";
    private static final String WMTS_URL = "http://localhost/wmts?Service=WMTS&Request=GetTile&Layer=X&TileMatrixSet=Z&TileMatrix=Z:1&TileRow=1&TileCol=1";
    private static final String LONG_URL = "https://dupa/z?=" + TestUtils.getRandomString(300);
    private static final String GENERIC_URL = "https://paka.pl/10/10/10.png";

    private static BufferedImage testImage;
    private static BufferedImage testImageOther;

    @BeforeEach
    void beforeEach() throws IOException {
        // delete test cache dir
        FileUtils.deleteDirectory(TEST_CACHE_DIR.toFile());
        testImage = ImageIO.read(TEST_IMAGE_FILE);
        testImageOther = ImageIO.read(TEST_IMAGE_OTHER_FILE);
    }

    @Test
    void testDefaultCacheSettings() {
        DirectoryBasedCache cache = new DirectoryBasedCache();
        assertThat(cache.getCacheTimeout()).isEqualTo(Defaults.DEFAULT_CACHE_TIMEOUT);
        assertThat(cache.getBaseDir()).isEqualTo(Defaults.DEFAULT_TILE_CACHE_DIR);
    }

    @Test
    void testCustomCacheSettings() {
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Duration.ofSeconds(2));
        assertThat(cache.getCacheTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(cache.getBaseDir()).isEqualTo(TEST_CACHE_DIR);
    }

    @Test
    void shouldNotCacheFileIfIOErrorOnWrite() {
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Duration.ofSeconds(2));

        try (MockedStatic<ImageIO> imageio = Mockito.mockStatic(ImageIO.class)) {
            imageio.when(() -> ImageIO.write(any(BufferedImage.class), any(), any(File.class))).thenThrow(IOException.class);
            // put in cache, this should fail now
            cache.put(XYZ_URL_WITH_QUERY, testImage);
        }
        // assert file not in cache
        assertThat(cache.get(XYZ_URL_WITH_QUERY)).isNull();

    }

    @Test
    void testCachePutWithDifferentUrlSchemas() {

        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
        cache.put(XYZ_URL_WITHOUT_QUERY, testImage);
        cache.put(XYZ_URL_WITH_QUERY, testImage);
        cache.put(WMTS_URL, testImage);

        retrieveFromCacheAndCheckContent(cache, XYZ_URL_WITHOUT_QUERY, testImage);
        retrieveFromCacheAndCheckContent(cache, XYZ_URL_WITH_QUERY, testImage);
        retrieveFromCacheAndCheckContent(cache, WMTS_URL, testImage);
    }
    @Test
    void testCacheGetPutWithTooLongKey() {
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
        // check too long cache key
        cache.put(LONG_URL, testImage);
        verifyLogged("File cache put failed for " + LONG_URL + "-" + "Cache key too long!");
        BufferedImage img = cache.get(LONG_URL);
        assertThat(img).isNull();
        verifyLogged("File cache get failed for " + LONG_URL + "-" + "Cache key too long!");
    }

    @Test
    void shouldCheckIfTileValidWhenCacheNonZeroAndNotCheckWhenZero() throws InterruptedException {
        // cache with default timeout
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
        // put some tile in - check if it is there and not expired
        cache.put(GENERIC_URL, testImage);
        cacheCheckExistenceAndExpiration(cache, GENERIC_URL, true, false);

        // now disable cache validity check
        cache.setCacheTimeout(Duration.ZERO);
        // it should always be valid (expired = false)
        assertThat(cache.keyExpired(GENERIC_URL)).isFalse();

        // now enable validity check with 2 seconds time
        cache.setCacheTimeout(Duration.ofSeconds(2));
        // after a second it should be stil there and valid (expired=false)
        ThreadUtils.sleep(Duration.ofSeconds(1));
        cacheCheckExistenceAndExpiration(cache, GENERIC_URL, true, false);

        // but now 3 sec  will pass. it should expire
        ThreadUtils.sleep(Duration.ofSeconds(2));
        cacheCheckExistenceAndExpiration(cache, GENERIC_URL, true, true);

    }


    @Test
    void testGetNonExisting() {
        // if getting file that does not exist in cache, get should return null
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
        String nonExistingUrl = "nonexisting";
        assertThat(cache.get(nonExistingUrl)).isNull();
    }

    @Test
    void testTimeoutZero() {
        // if cache timeout is zero, expiration check should never be called
        DirectoryBasedCache cache = spy(new DirectoryBasedCache(TEST_CACHE_DIR, Duration.ZERO));

        cache.put(GENERIC_URL, testImage);
        cache.get(GENERIC_URL);

        verify(cache, never()).keyExpired(GENERIC_URL);
    }

    @Test
    void shouldCheckCacheEntryValidity() {
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, SHORT_TIMEOUT_FOR_VALIDITY_TESTS);
        // should be valid right after putting in
        cache.put(GENERIC_URL, testImage);
        assertThat(cache.keyExpired(GENERIC_URL)).isFalse();
        // should not be valid after validity time has passed
        Awaitility.await().atMost(Duration.ofMillis(SHORT_TIMEOUT_FOR_VALIDITY_TESTS.toMillis() + 100)).
                untilAsserted(() -> assertThat(cache.keyExpired(GENERIC_URL)).isTrue());
    }

    @Test
    void shouldNeverInvalidateCacheEntryWhenCacheTimeoutZero() throws InterruptedException {
        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR,Duration.ZERO);
        // check for 2 seconds every 200ms
        // to check if it is always not expired when time passes
        long end = System.currentTimeMillis() + Duration.ofSeconds(2).toMillis();
        while (System.currentTimeMillis() < end ) {
            assertThat(cache.keyExpired(GENERIC_URL)).isFalse();
            ThreadUtils.sleep(Duration.ofMillis(200));
        }
    }

    @Test
    void shouldUpdateTileAtLocationIfDataChanged() {

        DirectoryBasedCache cache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
        cache.put(GENERIC_URL, testImage);
        retrieveFromCacheAndCheckContent(cache, GENERIC_URL, testImage);

        cache.put(GENERIC_URL, testImageOther);
        retrieveFromCacheAndCheckContent(cache, GENERIC_URL, testImageOther);
    }

    protected void retrieveFromCacheAndCheckContent(Cache<String,BufferedImage> cache, String url, BufferedImage img2) {
        assertThat(cache.get(url)).isNotNull();
        assertThat(ImageUtils.imagesEqual(cache.get(url), img2)).isTrue();
    }
}

