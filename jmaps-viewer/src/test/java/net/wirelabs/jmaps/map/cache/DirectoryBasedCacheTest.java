package net.wirelabs.jmaps.map.cache;

import net.wirelabs.jmaps.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import static net.wirelabs.jmaps.TestUtils.compareImages;
import static org.assertj.core.api.Assertions.*;

/**
 * Created 5/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class DirectoryBasedCacheTest {

    private static final File CACHE_DIR = new File("target/testcache/tile-cache");
    private static final File TEST_IMAGE_FILE = new File("src/test/resources/tiles/tile.png");
    private static final File TEST_IMAGE_OTHER_FILE = new File("src/test/resources/tiles/tile.png");
    private static final DirectoryBasedCache cache = new DirectoryBasedCache(CACHE_DIR.toPath().toString());

    private static BufferedImage TEST_IMAGE;
    private static BufferedImage TEST_IMAGE_OTHER;

    @BeforeEach
    void beforeEach() throws IOException {
        FileUtils.deleteDirectory(CACHE_DIR);
        TEST_IMAGE = ImageIO.read(TEST_IMAGE_FILE);
        TEST_IMAGE_OTHER = ImageIO.read(TEST_IMAGE_OTHER_FILE);
    }

    @Test
    void testCachePutXYZWithoutQuery() {

        String URL1 = "http://tile.openstreetmap.org/2/4/5.jpg";

        cache.put(URL1, TEST_IMAGE);

        retrieveFromCacheAndCheckContent(URL1, TEST_IMAGE);
    }

    @Test
    void testCachePutXYZWithQuery() {

        String URL2 = "http://tile.openstreetmap.org/2/4/5.jpg?apiKey=120931092";

        cache.put(URL2, TEST_IMAGE);

        retrieveFromCacheAndCheckContent(URL2, TEST_IMAGE);
    }

    @Test
    void testCachePutWMTS() {

        String URL3 = "http://localhost/wmts?Service=WMTS&Request=GetTile&Layer=X&TileMatrixSet=Z&TileMatrix=Z:1&TileRow=1&TileCol=1";

        cache.put(URL3, TEST_IMAGE);

        retrieveFromCacheAndCheckContent(URL3, TEST_IMAGE);
    }

    @Test
    void testCachePutLongUrl() {

        String LONG_URL ="https://dupa/z?="+TestUtils.getRandomString(300);

        cache.put(LONG_URL, TEST_IMAGE);

        retrieveFromCacheAndCheckContent(LONG_URL, TEST_IMAGE);
    }

    @Test
    void testGetNonExisting() {
        assertThat(cache.get("nonexisting")).isNull();
    }

    @Test
    void tesPutExistingShouldNotUpdate() {
        String URL = "https://paka.pl/10/10/10.png";
        cache.put(URL, TEST_IMAGE);
        retrieveFromCacheAndCheckContent(URL, TEST_IMAGE);
        //assertThat(cache.get(URL)).isNotNull();
        // put another image at this url should not succeed,
        // there should still be previous image in there
        cache.put(URL, TEST_IMAGE_OTHER);
        retrieveFromCacheAndCheckContent(URL, TEST_IMAGE);
    }

    void retrieveFromCacheAndCheckContent(String url, BufferedImage img2) {
        assertThat(cache.get(url)).isNotNull();
        assertThat(compareImages(cache.get(url), img2)).isTrue();
    }

}

