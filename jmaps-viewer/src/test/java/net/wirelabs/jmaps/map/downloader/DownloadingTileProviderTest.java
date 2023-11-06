package net.wirelabs.jmaps.map.downloader;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.files.DirectoryBasedCache;
import net.wirelabs.jmaps.map.cache.memory.InMemoryLRUCache;
import net.wirelabs.jmaps.map.utils.BaseTest;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.mockito.verification.VerificationMode;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created 11/10/23 by Michał Szwaczko (mikey@wirelabs.net)
 */

class DownloadingTileProviderTest extends BaseTest {

    private static final Path TEST_CACHE_DIR = new File("target/testcache").toPath();
    private static final File TEST_TILE_FILE = MockHttpServer.TEST_TILE_FILE;
    private static final Duration SHORT_TIMEOUT_FOR_VALIDITY_TESTS = Duration.ofSeconds(2);

    private final DirectoryBasedCache secondaryCache = new DirectoryBasedCache(TEST_CACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);

    private static MockHttpServer testTileServer;
    private static String tileUrl;
    private static String failTileUrl;

    private final MapViewer mapViewer = new MapViewer();
    private final DownloadingTileProvider tileProvider = spy(new DownloadingTileProvider(mapViewer));
    private final HttpClient mockHttpClient = generateMockHttpClient();
    private final DownloadingTileProvider tileProviderWithHttpClientMock = new DownloadingTileProvider(mapViewer, mockHttpClient);
    private InMemoryLRUCache primaryTileCache;

    @BeforeAll
    static void beforeAll() throws IOException {
        testTileServer = new MockHttpServer();
        tileUrl = "http://localhost:" + testTileServer.getPort() + "/tile.png";
        failTileUrl = "http://localhost:" +testTileServer.getPort() +"/nonexisting";
    }

    @BeforeEach
    void before() throws IOException {
        primaryTileCache = tileProvider.getPrimaryTileCache();
        mapViewer.setSecondaryTileCache(secondaryCache);
        FileUtils.deleteDirectory(TEST_CACHE_DIR.toFile());
    }

    @AfterAll
    static void after() {
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
        tileProvider.download(failTileUrl,failTileUrl);
        // assert dowload attempted
        assertDownloadCalled(times(1));
        // assert tile not in caches
        assertThat(primaryTileCache.get(tileUrl)).isNull();
        assertThat(secondaryCache.get(tileUrl)).isNull();
        verifyLogged("Could not download " + failTileUrl + " - Http response 404");

    }

    @Test
    void shouldNotDownloadTileIfItIsInPrimaryCache() throws IOException {

        primaryTileCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));

        assertThat(tileProvider.getTile(tileUrl,tileUrl)).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldPutFileInPrimaryCacheIfSecondaryCacheDisabled() {

        // no secondary cache, secondaryCacheEnabled() returns false
        mapViewer.setSecondaryTileCache(null);

        downloadTile();
        assertDownloadCalled(times(1));
        assertTileInPrimaryCache(tileUrl);

    }

    @Test
    void shouldNotDownloadTileIfItIsInTheSecondaryCache() throws IOException {

        // put imagefile to secondary cache
        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));

        BufferedImage tile = tileProvider.getTile(tileUrl,tileUrl);
        // get from secondary should update primary too
        assertTileInPrimaryCache(tileUrl);

        assertThat(tile).isNotNull();
        assertDownloadCalled(never());
    }

    @Test
    void shouldNotDownloadTileIfItIsNotExpired() throws IOException {

        // should not download file if it has not expired
        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));
        tileProvider.getTile(tileUrl,tileUrl);
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
    void shouldCheckNeverExpired() throws IOException {
        secondaryCache.setCacheTimeout(Duration.ZERO);
        secondaryCache.put(tileUrl, ImageIO.read(TEST_TILE_FILE));
        // check if tile not expired after waiting any validity time (plus some margin for test time)
        assertTileNotExpired();
        // download tile
        downloadTile();
        // assert it never happened - tile is valid no need to redownload
        assertDownloadCalled(never());

    }

    @Test
    void shouldTestHttpClientExceptions() throws IOException, InterruptedException {

        doThrow(new IOException("Error")).when(mockHttpClient).send(any(),any());
        tileProviderWithHttpClientMock.download(tileUrl,tileUrl);
        verifyLogged("Could not download " + tileUrl + " - IOException : Error");

        doThrow(new InterruptedException("Interrupted")).when(mockHttpClient).send(any(),any());
        tileProviderWithHttpClientMock.download(tileUrl,tileUrl);
        verifyLogged("Download interrupted for " + tileUrl);
    }

    @Test
    void shouldCatchOOMException() throws IOException, InterruptedException {
        doThrow(new OutOfMemoryError()).when(mockHttpClient).send(any(),any());
        tileProviderWithHttpClientMock.download(tileUrl,tileUrl);
        verifyLogged("DANG! Local memory cache run out of memory");
        verifyLogged("Pruning memory cache...");
    }


    private void assertTileInSecondaryCache(String tileUrl) {
        assertThat(secondaryCache.get(tileUrl)).isNotNull();
    }

    private void assertTileInPrimaryCache(String tileUrl) {
        assertThat(primaryTileCache.get(tileUrl)).isNotNull();
    }

    private void assertDownloadCalled(VerificationMode mode) {
        verify(tileProvider, mode).download(anyString(),anyString());
    }

    private void downloadTile() {
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(tileProvider.getTile(tileUrl,tileUrl)).isNotNull());
    }

    private void assertTileNotExpired() {
        Awaitility.await().pollDelay(SHORT_TIMEOUT_FOR_VALIDITY_TESTS.plus(Duration.ofMillis(1500))).untilAsserted(
                () -> assertThat(secondaryCache.keyExpired(tileUrl)).isFalse()
        );
    }

    private void assertTileExpired() {
        Awaitility.await().pollDelay(SHORT_TIMEOUT_FOR_VALIDITY_TESTS.plus(Duration.ofMillis(1500))).untilAsserted(
                () -> assertThat(secondaryCache.keyExpired(tileUrl)).isTrue()
        );
    }

    private HttpClient generateMockHttpClient() {
        return spy(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build());
    }
}