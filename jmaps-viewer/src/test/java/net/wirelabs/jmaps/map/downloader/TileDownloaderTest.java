package net.wirelabs.jmaps.map.downloader;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.NoArgsConstructor;
import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.MapViewer;
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

    private final DirectoryBasedCache secondaryCache = new DirectoryBasedCache(CACHE_DIR.getPath(), Defaults.DEFAULT_CACHE_TIMEOUT);
    private MockHttpServer testTileServer;
    private static final File CACHE_DIR = new File("target/testcache");
    private static final File TEST_TILE_FILE = MockHttpServer.TEST_TILE_FILE;
    private static final Duration SHORT_TIMEOUT_FOR_VALIDITY_TESTS = Duration.ofSeconds(2);

    private String tileUrl;
    private String failTileUrl;

    private final MapViewer mapViewer = new MapViewer();
    private final TileDownloader tileProvider = spy(new TileDownloader(mapViewer));
    private ConcurrentLinkedHashMap<String, BufferedImage> primaryCache;

    @BeforeEach
    void before() throws IOException {
        //mapViewer = new MapViewer();
        testTileServer = new MockHttpServer();
        primaryCache = tileProvider.primaryTileCache;
        tileProvider.setSecondaryTileCache(secondaryCache);
        tileUrl = "http://localhost:" + testTileServer.getPort() + "/tile.png";
        failTileUrl = "http://localhost:" +testTileServer.getPort() +"/nonexisting";
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
    void shouldNotDownloadNonExistingResource() {
        // download url that returns 404
        tileProvider.download(failTileUrl);
        // assert dowload attempted
        assertDownloadCalled(times(1));
        // assert tile not in caches
        assertThat(primaryCache.get(tileUrl)).isNull();
        assertThat(secondaryCache.get(tileUrl)).isNull();

    }



    @Test
    void shouldNotDownloadTileIfItIsInPrimaryCache() throws IOException {

        primaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));

        assertThat(tileProvider.getTile(tileUrl)).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldPutFileInPrimaryCacheIfSecondaryCacheDisabled() {

        // no secondary cache, secondaryCacheEnabled() returns false
        tileProvider.setSecondaryTileCache(null);

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
        // set short timeout for test
        secondaryCache.setCacheTimeout(SHORT_TIMEOUT_FOR_VALIDITY_TESTS);

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
        Awaitility.await().pollDelay(SHORT_TIMEOUT_FOR_VALIDITY_TESTS.plus(Duration.ofMillis(1500))).untilAsserted(
                () -> assertThat(secondaryCache.keyExpired(tileUrl)).isTrue()
        );
    }
}