package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.geo.Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class MapViewerTest {

    private static final File EXAMPLE_MAPFILE = new File("src/test/resources/maps/OpenStreetMap.xml");
    private static final File EXAMPLE_MAPFILE_DOUBLE_LAYER = new File("src/test/resources/maps/GeoportalLayered.xml");

    private static final Coordinate LUBLIN_PL = new Coordinate(22.565628, 51.247717);
    private static final String NEW_USER_AGENT = "Test-agent 1.0";
    private MapViewer mapviewer;
    private Point2D expectedTopLeftCornerPoint;

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
    void testCustomMapviewerInitialization() {
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

        mapviewer.setCurrentMap(EXAMPLE_MAPFILE_DOUBLE_LAYER);
        assertThat(mapviewer.getCurrentMap().layersPresent()).isTrue();
        assertThat(mapviewer.getCurrentMap().getEnabledLayers()).hasSize(2);
        assertThat(mapviewer.getCurrentMap().getEnabledLayers().get(0).getName()).isEqualTo("podklad");
        assertThat(mapviewer.getCurrentMap().getEnabledLayers().get(1).getName()).isEqualTo("cieniowanie");
    }

    @Test
    void testMapLoading() {

        // map without home set
        mapviewer.setCurrentMap(EXAMPLE_MAPFILE);
        expectedTopLeftCornerPoint = new Point2D.Double(mapviewer.getMapSizeInPixels(mapviewer.getZoom()).width / 2.0, mapviewer.getMapSizeInPixels(mapviewer.getZoom()).height / 2.0);
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