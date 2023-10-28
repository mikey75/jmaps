package net.wirelabs.jmaps.map.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.*;

/**
 * Created 5/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class DirectoryBasedCacheTest {

    private static final File CACHE_DIR = new File("/tmp/cache/tile-cache");

    private static final String URL1 = "http://tile.openstreetmap.org/2/4/5.jpg";
    private static final String EXPECTED_CACHE_KEY1 = "tile.openstreetmap.org/2/4/5.jpg";

    private static final String URL2 = "http://tile.openstreetmap.org/2/4/5.jpg?apiKey=120931092";
    private static final String EXPECTED_CACHE_KEY2 = "tile.openstreetmap.org/2/4/5.jpg$apiKey=120931092";

    private static final String URL3 = "http://localhost/wmts?Service=WMTS&Request=GetTile&Layer=G2_MOBILE_500&TileMatrixSet=EPSG:2180&TileMatrix=EPSG:2180:15&TileRow=11&TileCol=10";
    private static final String EXPECTED_CACHE_KEY3 = "localhost/wmts$Service=WMTS$Request=GetTile$Layer=G2_MOBILE_500$TileMatrixSet=EPSG$2180$TileMatrix=EPSG$2180$15$TileRow=11$TileCol=10";

    private static final File EXPECTED_XYZ_FILE1 = new File(CACHE_DIR, EXPECTED_CACHE_KEY1);
    private static final File EXPECTED_XYZ_FILE2 = new File(CACHE_DIR, EXPECTED_CACHE_KEY2);
    private static final File EXPECTED_WMTS_FILE = new File(CACHE_DIR, EXPECTED_CACHE_KEY3);

    private static final BufferedImage TEST_IMAGE = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);

    private static final DirectoryBasedCache cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString());

    @BeforeEach
    void before() throws IOException {

        Files.deleteIfExists(EXPECTED_XYZ_FILE1.toPath());
        Files.deleteIfExists(EXPECTED_XYZ_FILE2.toPath());
        Files.deleteIfExists(EXPECTED_WMTS_FILE.toPath());
    }

    @Test
    void testCachePutXYZWithoutQuery() {
        cache.put(URL1, TEST_IMAGE);
        assertThat(EXPECTED_XYZ_FILE1).exists().isFile();
    }

    @Test
    void testCachePutXYZWithQuery() {
        cache.put(URL2, TEST_IMAGE);
        assertThat(EXPECTED_XYZ_FILE2).exists().isFile();
    }
    @Test
    void testCacheKeyExists() {
        cache.put(URL2, TEST_IMAGE);
        assertThat(cache.contains(EXPECTED_XYZ_FILE2.toPath().toString())).isTrue();
    }

    @Test
    void testCachePutWMTS() {
        cache.put(URL3, TEST_IMAGE);
        assertThat(EXPECTED_WMTS_FILE).exists().isFile();
    }

    @Test
    void getExistingFromCache() {
        cache.put(URL3, TEST_IMAGE);
        assertThat(cache.get(EXPECTED_CACHE_KEY3)).isNotNull();
    }

    @Test
    void getNonExisting() {
        assertThat(cache.get("nonexisting")).isNull();
    }

    @Test
    void testUrlNormalizer() {

        String stringToNormalize =  "ab:x:3&a?a**<>\"";
        String expectedNormalized = "ab$x$3$a$a$$$$$";

        String result = cache.normalizeUrl(stringToNormalize);
        assertThat(result).isEqualTo(expectedNormalized);
    }
}

