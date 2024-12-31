package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.readers.WMTSCapReader;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class MapViewerTest  {

    private static final File EXAMPLE_MAPFILE = new File("src/test/resources/maps/OpenStreetMap.xml");
    private static final File EXAMPLE_MAPFILE_DOUBLE_LAYER = new File("src/test/resources/maps/GeoportalLayered.xml");

    private static final Coordinate LUBLIN_PL = new Coordinate(22.565628, 51.247717);
    private static final String NEW_USER_AGENT = "Test-agent 1.0";
    private MapViewer mapviewer;
    private MockHttpServer server;

    @BeforeEach
    void setup() {
        mapviewer = new MapViewer();
    }

    @Test
    void testDefaultMapviewerInitialization() {


        assertThat(mapviewer.getTilerThreads()).isEqualTo(Defaults.DEFAULT_TILER_THREADS);
        assertThat(mapviewer.getUserAgent()).isEqualTo(Defaults.DEFAULT_USER_AGENT);

        assertThat(mapviewer.isDeveloperMode()).isFalse();
        assertThat(mapviewer.isShowAttribution()).isTrue();
        assertThat(mapviewer.isShowCoordinates()).isFalse();

        assertThat(mapviewer.getZoom()).isEqualTo(3);
        assertThat(mapviewer.getCurrentMap().layersPresent()).isFalse();

    }

    @Test
    void testCustomMapviewerInitialization() throws IOException {

        mapviewer.setTilerThreads(10);
        mapviewer.setUserAgent(NEW_USER_AGENT);
        mapviewer.setShowAttribution(true);
        mapviewer.setShowCoordinates(true);
        mapviewer.setZoom(10);
        assertThat(mapviewer.getUserAgent()).isEqualTo(NEW_USER_AGENT);
        assertThat(mapviewer.getTilerThreads()).isEqualTo(10);

        mapviewer.setCurrentMap(EXAMPLE_MAPFILE);
        assertThat(mapviewer.getCurrentMap().layersPresent()).isTrue();
        assertThat(mapviewer.getCurrentMap().getEnabledLayers()).hasSize(1);
        assertThat(mapviewer.getCurrentMap().getBaseLayer().getName()).isEqualTo("Open Street Map");

        server = new MockHttpServer();
        WMTSCapReader.setCacheDir(Paths.get("target"));

        // fixup urls in mapDefinition to connect to test http server port
        File newFile = fixupUrls(EXAMPLE_MAPFILE_DOUBLE_LAYER);

        // set map with changed file
        mapviewer.setCurrentMap(newFile);

        assertThat(mapviewer.getCurrentMap().layersPresent()).isTrue();
        assertThat(mapviewer.getCurrentMap().getEnabledLayers()).hasSize(2);
        assertThat(mapviewer.getCurrentMap().getEnabledLayers().get(0).getName()).isEqualTo("podklad");
        assertThat(mapviewer.getCurrentMap().getEnabledLayers().get(1).getName()).isEqualTo("cieniowanie");
    }


    private File fixupUrls(File file) throws IOException {
        String lines = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        String replaced = lines.replaceAll("\\$\\{testport}", String.valueOf(server.getPort()));
        File newFile = File.createTempFile("test","test");
        FileUtils.writeStringToFile(newFile,replaced,StandardCharsets.UTF_8);
        return newFile;
    }

    @Test
    void testMapLoading() {

        // map without home set
        mapviewer.setCurrentMap(EXAMPLE_MAPFILE);
        Point2D expectedTopLeftCornerPoint = new Point2D.Double(mapviewer.getMapSizeInPixels(mapviewer.getZoom()).width / 2.0, mapviewer.getMapSizeInPixels(mapviewer.getZoom()).height / 2.0);
        assertTopLeftPointCorrect(false, expectedTopLeftCornerPoint);

        // map with home set
        mapviewer.setHome(LUBLIN_PL);
        mapviewer.setCurrentMap(EXAMPLE_MAPFILE);
        expectedTopLeftCornerPoint = mapviewer.getCurrentMap().getBaseLayer().latLonToPixel(LUBLIN_PL, mapviewer.getZoom());
        assertTopLeftPointCorrect(true , expectedTopLeftCornerPoint);

    }

    private  void assertTopLeftPointCorrect(boolean homeSet, Point2D expectedTLPoint) {
        if (!homeSet) {
            assertThat(mapviewer.getHome()).isNull();
        } else {
            assertThat(mapviewer.getHome()).isNotNull();
        }
        assertThat(mapviewer.getTopLeftCornerPoint().getLocation()).isEqualTo(new Point((int) (expectedTLPoint.getX() - mapviewer.getWidth() / 2.0), (int) (expectedTLPoint.getY() - mapviewer.getHeight() / 2.0)));
    }

}