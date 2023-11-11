package net.wirelabs.jmaps.map.downloader;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.TestUtils;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created 11/10/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class TileDownloaderTest {

    private TileDownloader tileProvider;
    private Cache<String, BufferedImage> secondaryCache;
    private TestHttpServer testTileServer;
    private String tileUrl;

    private static final File CACHE_DIR = new File("target/testcache");

    private static final File testTileFile = new File("src/test/resources/tiles/tile.png");
    private ConcurrentLinkedHashMap<String, BufferedImage> primaryCache;

    @BeforeEach
    void before() throws IOException {
        MapViewer mapViewer = new MapViewer();
        testTileServer = new TestHttpServer(testTileFile);
        tileProvider = spy(new TileDownloader(mapViewer));
        secondaryCache = new DirectoryBasedCache(CACHE_DIR.getPath());
        primaryCache = tileProvider.primaryTileCache;
        tileProvider.setSecondaryTileCache(secondaryCache);
        tileUrl = "http://localhost:" + testTileServer.getPort() + "/tile.png";
        FileUtils.deleteDirectory(CACHE_DIR);
    }

    @AfterEach
    void after() throws IOException {
        testTileServer.stop();
    }

    @Test
    void shouldDownloadTileAndUpdateCachesIfNotPreviouslyCached() {

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
                    assertThat(tileProvider.getTile(tileUrl)).isNotNull();
                });

        assertTileInSecondaryCache(tileUrl);
        assertTileInPrimaryCache(tileUrl);
        assertDownloadCalled(atLeastOnce());


    }

    @Test
    void shouldNotDownloadTileIfItIsInPrimaryCache() throws IOException {

        primaryCache.put(tileUrl, ImageIO.read(testTileFile));

        BufferedImage tile = tileProvider.getTile(tileUrl);

        assertThat(tile).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldNotDownloadTileIfItIsInTheSecondaryCache() throws IOException {
        // put imagefile to secondary cache
        secondaryCache.put(tileUrl, ImageIO.read(testTileFile));

        BufferedImage tile = tileProvider.getTile(tileUrl);
        // get from secondary should update primary too
        assertTileInPrimaryCache(tileUrl);

        assertThat(tile).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldDownloadAndUpdatePrimaryIfNoSecondaryCacheEnabled() {

        tileProvider.setSecondaryTileCache(null);

        Awaitility.await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(tileProvider.getTile(tileUrl)).isNotNull();
        });

        assertTileInPrimaryCache(tileUrl);
        assertDownloadCalled(atLeastOnce());
    }

    private void assertTileInSecondaryCache(String tileUrl) {
        assertThat(secondaryCache.get(tileUrl)).isNotNull();
    }

    private void assertTileInPrimaryCache(String tileUrl) {
        assertThat(primaryCache.get(tileUrl)).isNotNull();
    }

    private void assertDownloadCalled(VerificationMode mode) {
        verify(tileProvider, mode).download(anyString());
    }
}