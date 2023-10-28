package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.*;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class MapReaderTest {

    private static final File MAPFILE = new File("src/test/resources/map.xml");
    private static final File MAPFILE_BAD = new File("src/test/resources/map-bad.xml");
    private static final File MAPFILE_NONEXISTING = new File("nonexisting");

    private static final File TEST_CAPABILITIES_XML_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File EXPECTED_CACHED_FILE = new File("target/testcache/wmts-cache/localhost/wmts/capabilities.xml");

    @BeforeEach
    void deleteCacheFile() throws IOException {
        // delete cached file if any
        Files.deleteIfExists(EXPECTED_CACHED_FILE.toPath());
    }

    @Test
    void shouldLoadMapDefinitionFile() throws JAXBException {

        MapReader mapReader = new MapReader();

        MapDefinition m = mapReader.loadMapDefinitionFile(MAPFILE);

        assertThat(m.getName()).isEqualTo("Mapa 1");
        assertThat(m.getLayers()).hasSize(1);
        assertThat(m.getLayers().get(0).getUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(m.getLayers().get(0).getType()).isEqualTo(LayerType.XYZ);
        assertThat(m.getLayers().get(0).getMinZoom()).isEqualTo(3);
        assertThat(m.getLayers().get(0).getOpacity()).isEqualTo(1.0f);

    }

    @Test
    void shouldNotLoadBadMapDefinitionFile() {

        MapReader mapReader = new MapReader();

        assertThatExceptionOfType(UnmarshalException.class)
                .isThrownBy(() -> mapReader.loadMapDefinitionFile(MAPFILE_BAD))
                .withMessageContaining("unexpected element");

    }

    @Test
    void shouldNotLoadNonExistingMapDefinitionFile() {

        MapReader mapReader = new MapReader();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> mapReader.loadMapDefinitionFile(MAPFILE_NONEXISTING))
                .withMessageContaining("No such file or directory");
    }

    @Test
    void shouldLoadCapabilitieXMLFromNetwork() throws IOException {

        MapReader mapReader = new MapReader("target/testcache/wmts-cache");
        // setup fake web with capabilities.xml
        TestHttpServer server = new TestHttpServer(TEST_CAPABILITIES_XML_FILE);
        String testUrl = "http://localhost:" + server.getPort() + "/wmts";

        Capabilities caps = mapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(caps);
        server.stop();

    }

    @Test
    void shouldNotLoadNonexistingCapabilitiesURL() {
        MapReader mapReader = new MapReader("target/testcache/wmts-cache");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> mapReader.loadCapabilities("nonexisting"))
                .withMessageContaining("Could not parse Capablities.xml");
    }
    @Test
    void shouldLoadCachedCopyOfXMLCapabilities() throws IOException {

        MapReader mapReader = new MapReader("target/testcache/wmts-cache");
        Files.copy(TEST_CAPABILITIES_XML_FILE.toPath(), EXPECTED_CACHED_FILE.toPath());
        String testUrl = "http://localhost/wmts";

        Capabilities capabilities = mapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(capabilities);
    }

    private void assertFileIsCreatedAndHasCorrectContent(Capabilities capabilities) throws IOException {
        assertThat(capabilities).isNotNull();
        assertThat(EXPECTED_CACHED_FILE)
                .exists()
                .isFile()
                .hasContent(Files.readString(TEST_CAPABILITIES_XML_FILE.toPath(), StandardCharsets.UTF_8));
    }

}
