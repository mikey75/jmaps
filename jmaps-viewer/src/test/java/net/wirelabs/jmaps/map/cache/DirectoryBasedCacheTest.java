package net.wirelabs.jmaps.map.cache;

import net.wirelabs.jmaps.TestUtils;
import net.wirelabs.jmaps.map.Defaults;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static net.wirelabs.jmaps.TestUtils.compareImages;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 5/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class DirectoryBasedCacheTest {

    private static final File CACHE_DIR = new File("target/testcache/tile-cache");
    private static final Duration SHORT_TIMEOUT_FOR_VALIDITY_TESTS = Duration.ofSeconds(2);

    private static final File TEST_IMAGE_FILE = new File("src/test/resources/tiles/tile.png");
    private static final File TEST_IMAGE_OTHER_FILE = new File("src/test/resources/tiles/tile-other.png");

    private static BufferedImage TEST_IMAGE;
    private static BufferedImage TEST_IMAGE_OTHER;

    private static final String XYZ_URL_WITHOUT_QUERY = "http://tile.openstreetmap.org/2/4/5.jpg";
    private static final String XYZ_URL_WITH_QUERY = "http://tile.openstreetmap.org/2/4/5.jpg?apiKey=120931092";
    private static final String WMTS_URL = "http://localhost/wmts?Service=WMTS&Request=GetTile&Layer=X&TileMatrixSet=Z&TileMatrix=Z:1&TileRow=1&TileCol=1";
    private static final String LONG_URL = "https://dupa/z?=" + TestUtils.getRandomString(300);
    private static final String GENERIC_URL = "https://paka.pl/10/10/10.png";
    private static DirectoryBasedCache cache;

    @BeforeEach
    void init() throws IOException {

        FileUtils.deleteDirectory(CACHE_DIR);
        TEST_IMAGE = ImageIO.read(TEST_IMAGE_FILE);
        TEST_IMAGE_OTHER = ImageIO.read(TEST_IMAGE_OTHER_FILE);
    }

    @Test
    void testCachePutWithDifferentUrlSchemas() {

        cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString(), Defaults.DEFAULT_CACHE_TIMEOUT);
        cache.put(XYZ_URL_WITHOUT_QUERY, TEST_IMAGE);
        cache.put(XYZ_URL_WITH_QUERY, TEST_IMAGE);
        cache.put(WMTS_URL, TEST_IMAGE);
        cache.put(LONG_URL, TEST_IMAGE);

        retrieveFromCacheAndCheckContent(XYZ_URL_WITHOUT_QUERY, TEST_IMAGE);
        retrieveFromCacheAndCheckContent(XYZ_URL_WITH_QUERY, TEST_IMAGE);
        retrieveFromCacheAndCheckContent(WMTS_URL, TEST_IMAGE);
        retrieveFromCacheAndCheckContent(LONG_URL, TEST_IMAGE);

    }


    @Test
    void testGetNonExisting() {
        // if getting file that does not exist in cache, get should return null
        cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString(), Defaults.DEFAULT_CACHE_TIMEOUT);
        String NON_EXISTING_URL = "nonexisting";
        assertThat(cache.get(NON_EXISTING_URL)).isNull();
    }

    @Test
    void testTimeoutZero() {
        // if cache timeout is zero, expiration check should never be called
        cache = spy(new DirectoryBasedCache(CACHE_DIR.toPath().toString(), Duration.ZERO));

        cache.put(GENERIC_URL, TEST_IMAGE);
        cache.get(GENERIC_URL);

        verify(cache, never()).keyExpired(GENERIC_URL);
    }

    @Test
    void shouldCheckCacheEntryValidity() {
        cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString(), SHORT_TIMEOUT_FOR_VALIDITY_TESTS);
        // should be valid right after putting in
        cache.put(GENERIC_URL, TEST_IMAGE);
        assertThat(cache.keyExpired(GENERIC_URL)).isFalse();
        // should not be valid after validity time has passed
        Awaitility.await().atMost(Duration.ofMillis(SHORT_TIMEOUT_FOR_VALIDITY_TESTS.toMillis() + 100)).
                untilAsserted(() -> assertThat(cache.keyExpired(GENERIC_URL)).isTrue());
        // so cache.get() should return null now
        assertThat(cache.get(GENERIC_URL)).isNull();
    }


    @Test
    void shouldUpdateTileAtLocationIfDataChanged() {

        cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString(), Defaults.DEFAULT_CACHE_TIMEOUT);
        cache.put(GENERIC_URL, TEST_IMAGE);
        retrieveFromCacheAndCheckContent(GENERIC_URL, TEST_IMAGE);

        cache.put(GENERIC_URL, TEST_IMAGE_OTHER);
        retrieveFromCacheAndCheckContent(GENERIC_URL, TEST_IMAGE_OTHER);
    }

    protected void retrieveFromCacheAndCheckContent(String url, BufferedImage img2) {
        assertThat(cache.get(url)).isNotNull();
        assertThat(compareImages(cache.get(url), img2)).isTrue();
    }
}

