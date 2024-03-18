package net.wirelabs.jmaps.map.downloader;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import net.wirelabs.jmaps.TestHttpServer;
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
    private ConcurrentLinkedHashMap<String, BufferedImage> primaryCache;
    private Cache<String, BufferedImage> secondaryCache;

    private TestHttpServer testTileServer;
    private String tileUrl;

    private static final File CACHE_DIR = new File("target/testcache");
    private static final File TEST_TILE_FILE = new File("src/test/resources/tiles/tile.png");
    private static final Duration CACHE_VALIDITY_TIME = Duration.ofSeconds(2);

    @BeforeEach
    void before() throws IOException {
        MapViewer mapViewer = new MapViewer();
        testTileServer = new TestHttpServer(TEST_TILE_FILE);
        tileProvider = spy(new TileDownloader(mapViewer));

        primaryCache = tileProvider.primaryTileCache;
        secondaryCache = spy(new DirectoryBasedCache(CACHE_DIR.getPath()));
        when(secondaryCache.getValidityTime()).thenReturn(CACHE_VALIDITY_TIME);

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

        downloadTile();
        assertDownloadCalled(times(1));

        assertTileInSecondaryCache(tileUrl);
        assertTileInPrimaryCache(tileUrl);

    }



    @Test
    void shouldNotDownloadTileIfItIsInPrimaryCache() throws IOException {

        primaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));
        assertThat(tileProvider.getTile(tileUrl)).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldPutFileInPrimaryCacheIfSecondaryCacheDisabled() {

        secondaryCache = null; // secondaryCacheEnabled() zwróci false
        downloadTile();
        assertDownloadCalled(times(1));
        assertTileInPrimaryCache(tileUrl);

    }


    @Test
    void shouldNotDownloadTileIfItIsInTheSecondaryCache() throws IOException {
        // put imagefile to secondary cache
        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));

        BufferedImage tile = tileProvider.getTile(tileUrl);
        // get from secondary should update primary too
        assertTileInPrimaryCache(tileUrl);

        assertThat(tile).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldNotDownloadTileIfItIsNotExpired() throws IOException {
        // should not download file if it has not expired
        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));
        tileProvider.getTile(tileUrl);
        assertDownloadCalled(never());
    }

    @Test
    void shouldDownloadTileIfItIsExpired() throws IOException {

        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));
        // check if tile expired after waiting validity time (plus some margin for test time)
        assertTileExpired();
        // download tile
        downloadTile();
        assertDownloadCalled(times(1));
    }



    @Test
    void shouldDownloadAndUpdatePrimaryIfNoSecondaryCacheEnabled() {

        tileProvider.setSecondaryTileCache(null);
        downloadTile();
        assertDownloadCalled(times(1));
        assertTileInPrimaryCache(tileUrl);
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

    private void downloadTile() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            assertThat(tileProvider.getTile(tileUrl)).isNotNull();
        });
    }

    private void assertTileExpired() {
        Awaitility.await().pollDelay(CACHE_VALIDITY_TIME.plus(Duration.ofMillis(1500))).untilAsserted(
                () -> assertThat(secondaryCache.keyExpired(tileUrl)).isTrue()
        );
    }
}