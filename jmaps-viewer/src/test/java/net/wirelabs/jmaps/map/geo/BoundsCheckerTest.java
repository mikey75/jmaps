package net.wirelabs.jmaps.map.geo;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.utils.BaseTest;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoundsCheckerTest extends BaseTest {

    @Test
    void shouldCheckBounds() throws IOException {
        // setup mock server
        MockHttpServer server = new MockHttpServer();

        // use non default cache and host
        String epsgHost = "http://localhost:" + server.getPort() + "/";
        Path cacheDir = Path.of(System.getProperty("java.io.tmpdir"), "testCache");

        BoundsChecker boundsCheck = new BoundsChecker(epsgHost, cacheDir);


        List<Coordinate> plCoords = List.of(new Coordinate(22.2139, 51.2418)); // polish coords
        List<Coordinate> czechCoord = List.of(new Coordinate(12.8640, 49.9819)); // czech coords

        // Polish  coords on Polish epsg - should return false
        assertThat(boundsCheck.isTrackOutOfBand(plCoords, "EPSG:2180")).isFalse();

        // Czech coords on Polish epsg - should return true
        assertThat(boundsCheck.isTrackOutOfBand(czechCoord, "EPSG:2180")).isTrue();

        // Czech and Polish coords on WebMerkator  - both should be false
        assertThat(boundsCheck.isTrackOutOfBand(czechCoord, "EPSG:3857")).isFalse();
        assertThat(boundsCheck.isTrackOutOfBand(plCoords, "EPSG:3857")).isFalse();

        // get nonexisting epsg
        boundsCheck.isTrackOutOfBand(plCoords, "EPSG:0001");

        verifyLogged("http call failed. Assuming not out of band");

        // delete cache
        FileUtils.deleteDirectory(Path.of(System.getProperty("java.io.tmpdir"), "testCache").toFile());
        server.stop();
    }
}