package net.wirelabs.jmaps.map.readers;

import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WMTSCapReaderTest {

    private static final String TEST_CACHE_ROOT = "target/testcache/wmts-cache";

    private static final File TEST_CAPABILITIES_XML_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File EXPECTED_CACHED_FILE = new File(TEST_CACHE_ROOT,"localhost/wmts/capabilities.xml");
    private static final File NON_CAPABILITIES_FILE = new File("src/test/resources/wmts/non-capabilities.xml");

    @BeforeEach
    void deleteCacheFile() throws IOException {
        // delete cached file if any
        Files.deleteIfExists(EXPECTED_CACHED_FILE.toPath());
        // set test cache dir
        WMTSCapReader.setCacheDir(TEST_CACHE_ROOT);
    }


    @Test
    void shouldLoadCapabilitieXMLFromNetwork() throws IOException {

        // setup fake web with capabilities.xml
        TestHttpServer server = new TestHttpServer(TEST_CAPABILITIES_XML_FILE);
        String testUrl = "http://localhost:" + server.getPort() + "/wmts";

        Capabilities caps = WMTSCapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(caps);
        server.stop();

    }

    @Test
    void shouldNotLoadNonexistingCapabilitiesURL() {

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> WMTSCapReader.loadCapabilities("nonexisting"))
                .withMessageContaining("Could not parse Capabilities.xml");
    }

    @Test
    void shouldLoadCachedCopyOfXMLCapabilities() throws IOException {

        Files.copy(TEST_CAPABILITIES_XML_FILE.toPath(), EXPECTED_CACHED_FILE.toPath());
        String testUrl = "http://localhost/wmts";
        Capabilities capabilities = WMTSCapReader.loadCapabilities(testUrl);
        assertFileIsCreatedAndHasCorrectContent(capabilities);
    }

    @Test
    void shouldNotLoadAndCacheNonXMLFileFromNetwork() throws Exception {
        TestHttpServer server = new TestHttpServer(NON_CAPABILITIES_FILE);
        String testUrl = "http://localhost:" + server.getPort() + "/wmts";

        assertThatExceptionOfType(CriticalMapException.class).isThrownBy(() -> WMTSCapReader.loadCapabilities(testUrl))
                .withMessageContaining("Could not parse Capabilities.xml");

        assertThat(EXPECTED_CACHED_FILE).doesNotExist();

    }

    private void assertFileIsCreatedAndHasCorrectContent(Capabilities capabilities) throws IOException {
        assertThat(capabilities).isNotNull();
        assertThat(EXPECTED_CACHED_FILE)
                .exists()
                .isFile()
                .hasContent(Files.readString(TEST_CAPABILITIES_XML_FILE.toPath(), StandardCharsets.UTF_8));
    }

}
