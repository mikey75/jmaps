package net.wirelabs.jmaps.map.readers;

import net.opengis.wmts.x10.CapabilitiesDocument;
import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CapabilitiesReaderTest {

    private static final Path TEST_CACHE_ROOT = Path.of("target/testcache/wmts-cache");

    private static final File VALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File EXPECTED_VALID_CACHED_FILE = new File(TEST_CACHE_ROOT.toString(),"localhost/valid1/capabilities.xml");

    private MockHttpServer server;

    @BeforeEach
    void deleteCacheFile() throws IOException {
        // delete cached file if any
        Files.deleteIfExists(EXPECTED_VALID_CACHED_FILE.toPath());

        // setup fake web with capabilities.xml
        // set test cache dir
        WMTSCapReader.setCacheDir(TEST_CACHE_ROOT);
        // setup mock server
        server = new MockHttpServer();
    }

    @AfterEach
    void after() throws IOException {
        if (server!=null) {
            server.stop();
        }
    }

    @Test
    void shouldLoadCapabilitieXMLFromNetwork() throws IOException {


        String testUrl = "http://localhost:" + server.getPort() +"/valid1";

        CapabilitiesDocument.Capabilities caps = WMTSCapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(caps);
        server.stop();

    }

    @Test
    void shouldNotLoadAndCacheNonXMLFileFromNetwork()  {

        String testUrl = "http://localhost:" + server.getPort()+ "/invalid";

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> WMTSCapReader.loadCapabilities(testUrl))
                .withMessageContaining("Could not parse WMTS capabilities from " + testUrl);

        assertThat(EXPECTED_VALID_CACHED_FILE).doesNotExist();

    }

    @Test
    void shouldNotLoadNonexistingCapabilitiesURL() {
        String testUrl = "http://localhost:"+ server.getPort()+"/nonexisting";
        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> WMTSCapReader.loadCapabilities(testUrl))
                .withMessageContaining("Could not parse WMTS capabilities from "+ testUrl);
    }

    @Test
    void shouldLoadCachedCopyOfXMLCapabilities() throws IOException {

        Files.copy(VALID_CAPABILITIES_FILE.toPath(), EXPECTED_VALID_CACHED_FILE.toPath());
        String testUrl = "http://localhost:"+server.getPort()+"/valid1";
        CapabilitiesDocument.Capabilities capabilities = WMTSCapReader.loadCapabilities(testUrl);
        assertFileIsCreatedAndHasCorrectContent(capabilities);
    }

    private void assertFileIsCreatedAndHasCorrectContent(CapabilitiesDocument.Capabilities capabilities) throws IOException {
        assertThat(capabilities).isNotNull();
        assertThat(EXPECTED_VALID_CACHED_FILE)
                .exists()
                .isFile()
                .hasContent(Files.readString(VALID_CAPABILITIES_FILE.toPath(), StandardCharsets.UTF_8));
    }

}
