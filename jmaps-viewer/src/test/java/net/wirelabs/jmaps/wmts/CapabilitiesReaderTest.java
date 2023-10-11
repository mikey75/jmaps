package net.wirelabs.jmaps.wmts;


import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import net.wirelabs.jmaps.map.utils.MapReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;


class CapabilitiesReaderTest {

    private final File testCapabilities = new File("src/test/resources/wmts/capabilities.xml");
    private final File expectedCachedFile = new File("target/testcache/wmts-cache/localhost/wmts/capabilities.xml");
    private MapReader mapReader;

    @BeforeEach
    void before() throws IOException {
        mapReader = new MapReader("target/testcache/wmts-cache");
        // delete cached file if any -> simulate file is not in cache
        Files.deleteIfExists(expectedCachedFile.toPath());
    }


    @Test
    void shouldLoadFromNetwork() throws IOException {
        // setup fake web with capabilities.xml
        TestHttpServer server = new TestHttpServer(TestHttpServer.getRandomFreeTcpPort(), testCapabilities);
        String testUrl = "http://localhost:"+ server.getListeningPort()+"/wmts";
        // delete cached file if any -> simulate file is not in cache
        Files.deleteIfExists(expectedCachedFile.toPath());


        Capabilities caps = mapReader.loadCapabilities(testUrl);
        assertFileIsCreatedAndHasCorrectContent(caps);
        server.stop();

    }


    @Test
    void shouldLoadCachedCopy() throws IOException {
        // dont start server,  create cache file -> simulate it already exists
        Files.copy(testCapabilities.toPath(), expectedCachedFile.toPath());
        String testUrl = "http://localhost/wmts";
        //
       // CapabilitiesReader cr = new CapabilitiesReader();
        Capabilities caps = mapReader.loadCapabilities(testUrl);

        assertFileIsCreatedAndHasCorrectContent(caps);
    }

    private void assertFileIsCreatedAndHasCorrectContent(Capabilities caps) throws IOException {
        Assertions.assertThat(caps).isNotNull();
        assertThat(expectedCachedFile).exists().isFile().hasContent(Files.readString(testCapabilities.toPath(), StandardCharsets.UTF_8));
    }

}
