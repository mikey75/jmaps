package net.wirelabs.jmaps.map.cache;

import net.wirelabs.jmaps.TestUtils;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created 5/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class DirectoryBasedCacheTest {

    private static final File CACHE_DIR = new File("target/testcache/tile-cache");
    private static final Cache<String, BufferedImage> cache = spy(new DirectoryBasedCache(CACHE_DIR.toPath().toString()));
    private static final Duration validityTime = Duration.ofSeconds(2);

    private static final File TEST_IMAGE_FILE = new File("src/test/resources/tiles/tile.png");
    private static final File TEST_IMAGE_OTHER_FILE = new File("src/test/resources/tiles/tile-other.png");

    private static BufferedImage TEST_IMAGE;
    private static BufferedImage TEST_IMAGE_OTHER;

    private static final String XYZ_URL_WITHOUT_QUERY = "http://tile.openstreetmap.org/2/4/5.jpg";
    private static final String XYZ_URL_WITH_QUERY = "http://tile.openstreetmap.org/2/4/5.jpg?apiKey=120931092";
    private static final String WMTS_URL = "http://localhost/wmts?Service=WMTS&Request=GetTile&Layer=X&TileMatrixSet=Z&TileMatrix=Z:1&TileRow=1&TileCol=1";
    private static final String LONG_URL = "https://dupa/z?=" + TestUtils.getRandomString(300);
    private static final String GENERIC_URL = "https://paka.pl/10/10/10.png";

    @BeforeEach
    void init() throws IOException {

        FileUtils.deleteDirectory(CACHE_DIR);
        when(cache.getValidityTime()).thenReturn(validityTime);
        TEST_IMAGE = ImageIO.read(TEST_IMAGE_FILE);
        TEST_IMAGE_OTHER = ImageIO.read(TEST_IMAGE_OTHER_FILE);
    }

    @Test
    void testCachePutWithDifferentUrlSchemas() {

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
        String NON_EXISTING_URL = "nonexisting";
        assertThat(cache.get(NON_EXISTING_URL)).isNull();
    }

    @Test
    void shouldCheckCacheEntryValidity() {
        // should be valid right after putting in
        cache.put(GENERIC_URL, TEST_IMAGE);
        assertThat(cache.keyExpired(GENERIC_URL)).isFalse();
        // should not be valid after validity time has passed
        Awaitility.await().atMost(Duration.ofMillis(validityTime.toMillis() + 100)).
                untilAsserted(() -> assertThat(cache.keyExpired(GENERIC_URL)).isTrue());
    }


    @Test
    void shouldUpdateTileAtLocationIfDataChanged() {

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

