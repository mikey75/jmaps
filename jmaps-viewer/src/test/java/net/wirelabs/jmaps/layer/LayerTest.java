package net.wirelabs.jmaps.layer;


import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.WMTSLayer;
import net.wirelabs.jmaps.map.layer.XYZLayer;
import net.wirelabs.jmaps.viewer.geo.Coordinate;
import net.wirelabs.jmaps.viewer.geo.ProjectionEngine;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class LayerTest {

    private String testUrl;

    @BeforeEach
    void before() throws IOException {

        // serve fake capabilities
        File testCapabilitiesFile = new File("src/test/resources/wmts/capabilities.xml");
        TestHttpServer server = new TestHttpServer(TestHttpServer.getRandomFreeTcpPort(), testCapabilitiesFile);
        testUrl = "http://localhost:"+ server.getListeningPort()+"/wmts";

    }

    @Test
    void shouldProperlyInitializeDefaultXYZLayer()  {

        // xyz layer with defaults
        Layer xyz = new XYZLayer("TestXYZ", "http://localhost/{z}/{x}/{y}.png");

        Assertions.assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:3857"); // assert default crs, we did not set
        Assertions.assertThat(xyz.getTileSize()).isEqualTo(256);
        Assertions.assertThat(xyz.getMaxZoom()).isEqualTo(18);
        Assertions.assertThat(xyz.getMinZoom()).isZero();
        Assertions.assertThat(xyz.getMapSize(0).height).isEqualTo(1);
        Assertions.assertThat(xyz.getMapSize(0).width).isEqualTo(1);
        Assertions.assertThat(xyz.createTileUrl(10, 11, 18)).isEqualTo("http://localhost/18/10/11.png");

    }

    @Test
    void shouldProperlyInitializeCustomXYZLayer() throws IOException {

        // xyz layer with custom params
        Layer xyz = new XYZLayer("TestXYZ", "http://localhost/{z}/{x}/{y}.png");
        xyz.setTileSize(512);
        xyz.setMaxZoom(10);
        xyz.setMinZoom(4);
        xyz.setOpacity(0.8f);
        xyz.setZoomOffset(1);


        Assertions.assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:3857"); // assert default crs, we did not set
        Assertions.assertThat(xyz.getTileSize()).isEqualTo(512);
        Assertions.assertThat(xyz.getMaxZoom()).isEqualTo(10);
        Assertions.assertThat(xyz.getMinZoom()).isEqualTo(4);
        Assertions.assertThat(xyz.getMapSize(0).height).isEqualTo(1);
        Assertions.assertThat(xyz.getMapSize(0).width).isEqualTo(1);
        Assertions.assertThat(xyz.createTileUrl(10, 11, 18)).isEqualTo("http://localhost/18/10/11.png");

    }

    @Test
    void shouldProperlyInitializeDefaultWMTSLayer() {
        // example of wmts map
        Layer wmts = new WMTSLayer("TestWmts", testUrl);

        Assertions.assertThat(wmts.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180");
        Assertions.assertThat(wmts.getMaxZoom()).isEqualTo(15);
        Assertions.assertThat(wmts.getMinZoom()).isZero();
        Assertions.assertThat(wmts.getTileSize()).isEqualTo(512);
        Assertions.assertThat(wmts.getMapSize(0).height).isEqualTo(1);
        Assertions.assertThat(wmts.getMapSize(0).width).isEqualTo(1);


        Assertions.assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                testUrl +
                        "?Service=WMTS" +
                        "&Request=GetTile" +
                        "&Layer=G2_MOBILE_500" +
                        "&Version=1.0.0&format=image/png&style=default" +
                        "&TileMatrixSet=EPSG:2180" +
                        "&TileMatrix=EPSG:2180:15" +
                        "&TileRow=11&TileCol=10"
        );

    }

    @Test
    void shouldProperlyInitializeCustomWMTSLayer() {
        // example of wmts map
        Layer wmts = new WMTSLayer("TestWmts", testUrl, "A2", "SET1");
        wmts.setMinZoom(3);


        Assertions.assertThat(wmts.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180");
        Assertions.assertThat(wmts.getMaxZoom()).isEqualTo(15);
        Assertions.assertThat(wmts.getMinZoom()).isEqualTo(3);
        Assertions.assertThat(wmts.getTileSize()).isEqualTo(512);
        Assertions.assertThat(wmts.getMapSize(0).height).isEqualTo(1);
        Assertions.assertThat(wmts.getMapSize(0).width).isEqualTo(1);


        Assertions.assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                testUrl +
                        "?Service=WMTS" +
                        "&Request=GetTile" +
                        "&Layer=A2" +
                        "&Version=1.0.0&format=image/png&style=default" +
                        "&TileMatrixSet=SET1" +
                        "&TileMatrix=EPSG:2180:15" +
                        "&TileRow=11&TileCol=10"
        );

    }

    @Test
    void shouldSetupSpecifiedCRS() {
        Layer layer = new XYZLayer("x", "http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        layer.setProjectionEngine(new ProjectionEngine("ESRI:2481"));
        Assertions.assertThat(layer.getProjectionEngine().getCrs().getName()).isEqualTo("ESRI:2481");

        layer = new WMTSLayer("x", "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/G2_MOBILE_500");
        layer.setProjectionEngine(new ProjectionEngine("ESri:32620"));
        Assertions.assertThat(layer.getProjectionEngine().getCrs().getName()).isEqualTo("ESri:32620");

    }

    @Test
    void shouldConvert() {
        Coordinate lublin = new Coordinate(22.4900397, 51.2326363);

        Layer xyz = new DefaultXYZLayer();
        Point2D c = xyz.latLonToPixel(lublin, 3);

        xyz.pixelToLatLon(c, 3);

    }

}