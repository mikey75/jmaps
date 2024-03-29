package net.wirelabs.jmaps.map.readers;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CapabilitiesReaderTest {

    private static final String TEST_CACHE_ROOT = "target/testcache/wmts-cache";

    private static final File VALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File EXPECTED_VALID_CACHED_FILE = new File(TEST_CACHE_ROOT,"localhost/valid1/capabilities.xml");

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


        String testUrl = "http://localhost:55555/valid1";

        Capabilities caps = WMTSCapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(caps);
        server.stop();

    }

    @Test
    void shouldNotLoadAndCacheNonXMLFileFromNetwork() throws Exception {

        String testUrl = "http://localhost:55555/invalid";

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> WMTSCapReader.loadCapabilities(testUrl))
                .withMessageContaining("Could not parse Capabilities.xml");

        assertThat(EXPECTED_VALID_CACHED_FILE).doesNotExist();

    }

    @Test
    void shouldNotLoadNonexistingCapabilitiesURL() {
        String testUrl = "http://localhost:55555/nonexisting";
        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> WMTSCapReader.loadCapabilities(testUrl))
                .withMessageContaining("Could not parse Capabilities.xml");
    }

    @Test
    void shouldLoadCachedCopyOfXMLCapabilities() throws IOException {

        Files.copy(VALID_CAPABILITIES_FILE.toPath(), EXPECTED_VALID_CACHED_FILE.toPath());
        String testUrl = "http://localhost:55555/valid1";
        Capabilities capabilities = WMTSCapReader.loadCapabilities(testUrl);
        assertFileIsCreatedAndHasCorrectContent(capabilities);
    }

    private void assertFileIsCreatedAndHasCorrectContent(Capabilities capabilities) throws IOException {
        assertThat(capabilities).isNotNull();
        assertThat(EXPECTED_VALID_CACHED_FILE)
                .exists()
                .isFile()
                .hasContent(Files.readString(VALID_CAPABILITIES_FILE.toPath(), StandardCharsets.UTF_8));
    }

}
