package net.wirelabs.jmaps.map.downloader;

import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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


    private static final File testTileFile = new File("src/test/resources/tiles/tile.png");
    private static final File cachedTileFile = new File("target/testcache/localhost/tile.png");

    @BeforeEach
    void before() throws IOException {
        MapViewer mapViewer = new MapViewer();
        testTileServer = new TestHttpServer(testTileFile);
        tileProvider = spy(new TileDownloader(mapViewer));
        secondaryCache = new DirectoryBasedCache("target/testcache/");
        tileProvider.setSecondaryTileCache(secondaryCache);
        tileUrl = "http://localhost:" + testTileServer.getPort() + "/tile.png";
        Files.deleteIfExists(cachedTileFile.toPath());
    }

    @AfterEach
    void after() throws IOException {
        testTileServer.stop();
    }

    @Test
    void getTileNotCachedBefore() {

        assertTileNotInAnyCache();

        Awaitility.await().atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    assertThat(tileProvider.getTile(tileUrl)).isNotNull();
                    assertFileInSecondaryCache();
                    assertFileInPrimaryCache();
                    assertDownloadCalled(atLeastOnce());
                });


    }

    @Test
    void getTileCachedOnSecondaryCache() throws IOException {
        // put imagefile to secondary cache
        Files.copy(testTileFile.toPath(), cachedTileFile.toPath());
        assertThat(cachedTileFile).isFile().exists();

        BufferedImage tile = tileProvider.getTile(tileUrl);

        assertThat(tile).isNotNull();
        assertDownloadCalled(never());
    }

    private void assertTileNotInAnyCache() {
        assertThat(cachedTileFile).doesNotExist();
        assertThat(tileProvider.isTileInCache(tileUrl)).isFalse();
    }

    private void assertFileInSecondaryCache() {
        assertThat(secondaryCache.get(tileUrl)).isNotNull();
    }

    private void assertFileInPrimaryCache() {
        assertThat(tileProvider.isTileInCache(tileUrl)).isTrue();
    }

    private void assertDownloadCalled(VerificationMode mode) {
        verify(tileProvider, mode).download(anyString());
    }
}