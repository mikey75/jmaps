package net.wirelabs.jmaps.map.layer;


import net.wirelabs.jmaps.TestHttpServer;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import static net.wirelabs.jmaps.TestUtils.roundDouble;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class LayerTest {

    private String testUrl;
    private LayerDefinition xyzLayerDefinition;
    private LayerDefinition wmtsLayerDefinition;

    @BeforeEach
    void before() throws IOException {

        // serve fake capabilities
        File testCapabilitiesFile = new File("src/test/resources/wmts/capabilities.xml");
        TestHttpServer server = new TestHttpServer(testCapabilitiesFile);
        testUrl = "http://localhost:" + server.getPort() + "/wmts";

        xyzLayerDefinition = new LayerDefinition("TestXYZ","http://localhost/{z}/{x}/{y}.png");
        wmtsLayerDefinition = new LayerDefinition("TestWmts", testUrl);
    }

    @Test
    void shouldProperlyInitializeDefaultXYZLayer() {

        // xyz layer with defaults
        Layer xyz = new XYZLayer(xyzLayerDefinition);

        assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:3857"); // assert default crs, we did not set
        assertThat(xyz.getTileSize()).isEqualTo(256);
        assertThat(xyz.getMaxZoom()).isEqualTo(18);
        assertThat(xyz.getMinZoom()).isZero();
        assertThat(xyz.getMapSize(0).height).isEqualTo(1);
        assertThat(xyz.getMapSize(0).width).isEqualTo(1);
        assertThat(xyz.createTileUrl(10, 11, 18)).isEqualTo("http://localhost/18/10/11.png");

    }

    @Test
    void shouldProperlyInitializeCustomXYZLayer() throws IOException {

        // xyz layer with custom params
        xyzLayerDefinition.setMaxZoom(10);
        xyzLayerDefinition.setMinZoom(4);
        xyzLayerDefinition.setOpacity(0.8f);
        xyzLayerDefinition.setZoomOffset(1);
        xyzLayerDefinition.setSwapAxis(true);
        Layer xyz = new XYZLayer(xyzLayerDefinition);


        assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:3857"); // assert default crs, we did not set
        assertThat(xyz.getTileSize()).isEqualTo(256);
        assertThat(xyz.getMaxZoom()).isEqualTo(10);
        assertThat(xyz.getMinZoom()).isEqualTo(4);
        assertThat(xyz.isSwapAxis()).isTrue();
        assertThat(xyz.getMapSize(0).height).isEqualTo(1);
        assertThat(xyz.getMapSize(0).width).isEqualTo(1);
        assertThat(xyz.createTileUrl(10, 11, 18)).isEqualTo("http://localhost/18/10/11.png");

    }

    @Test
    void shouldProperlyInitializeDefaultWMTSLayer() {
        // example of wmts map
        Layer wmts = new WMTSLayer(wmtsLayerDefinition);

        assertThat(wmts.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180");
        assertThat(wmts.getMaxZoom()).isEqualTo(15);
        assertThat(wmts.getMinZoom()).isZero();
        assertThat(wmts.getTileSize()).isEqualTo(512);
        assertThat(wmts.getMapSize(0).height).isEqualTo(1);
        assertThat(wmts.getMapSize(0).width).isEqualTo(1);
        assertThat(wmts.isSwapAxis()).isFalse();


        assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                HttpUrl.parse(testUrl).newBuilder()
                        .addQueryParameter("Service", "WMTS")
                        .addQueryParameter("Request", "GetTile")
                        .addQueryParameter("Layer", "G2_MOBILE_500")
                        .addQueryParameter("Version", "1.0.0")
                        .addQueryParameter("format", "image/png")
                        .addQueryParameter("style", "default")
                        .addQueryParameter("TileMatrixSet", "EPSG:2180")
                        .addQueryParameter("TileMatrix", "EPSG:2180:15")
                        .addQueryParameter("TileRow", "11")
                        .addQueryParameter("TileCol", "10")
                        .toString()

        );

    }

    @Test
    void shouldProperlyInitializeCustomWMTSLayer() {
        // example of wmts map
        wmtsLayerDefinition.setTileMatrixSet("EPSG:2180");
        wmtsLayerDefinition.setWmtsLayer("A2");
        wmtsLayerDefinition.setMinZoom(3);
        wmtsLayerDefinition.setSwapAxis(true);
        Layer wmts = new WMTSLayer(wmtsLayerDefinition);



        assertThat(wmts.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180");
        assertThat(wmts.getMaxZoom()).isEqualTo(15);
        assertThat(wmts.getMinZoom()).isEqualTo(3);
        assertThat(wmts.getTileSize()).isEqualTo(512);
        assertThat(wmts.isSwapAxis()).isTrue();
        assertThat(wmts.getMapSize(0).height).isEqualTo(1);
        assertThat(wmts.getMapSize(0).width).isEqualTo(1);


        assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                HttpUrl.parse(testUrl).newBuilder()
                        .addQueryParameter("Service","WMTS")
                        .addQueryParameter("Request","GetTile")
                        .addQueryParameter("Layer","A2")
                        .addQueryParameter("Version","1.0.0")
                        .addQueryParameter("format","image/png")
                        .addQueryParameter("style","default")
                        .addQueryParameter("TileMatrixSet","EPSG:2180")
                        .addQueryParameter("TileMatrix","EPSG:2180:15")
                        .addQueryParameter("TileRow","11")
                        .addQueryParameter("TileCol","10")
                        .toString());

    }

    @Test
    void shouldSetupSpecifiedCRS() {
        Layer layer = new XYZLayer(xyzLayerDefinition);
        layer.setProjectionEngine(new ProjectionEngine("ESRI:2481"));
        assertThat(layer.getProjectionEngine().getCrs().getName()).isEqualTo("ESRI:2481");

        layer = new WMTSLayer(wmtsLayerDefinition);
        layer.setProjectionEngine(new ProjectionEngine("ESri:32620"));
        assertThat(layer.getProjectionEngine().getCrs().getName()).isEqualTo("ESri:32620");

    }

    @Test
    void shouldConvertToPixelAndBack() {

        Layer xyz = new XYZLayer(xyzLayerDefinition);
        Coordinate lublin = new Coordinate(22.4900397, 51.2326363);

        Point2D pixel = xyz.latLonToPixel(lublin, 3);
        Coordinate pixelConvertedBackToLatLon = xyz.pixelToLatLon(pixel, 3);

        assertThat(roundDouble(pixelConvertedBackToLatLon.getLongitude(), 7)).isEqualTo(lublin.getLongitude());
        assertThat(roundDouble(pixelConvertedBackToLatLon.getLatitude(), 7)).isEqualTo(lublin.getLatitude());

    }

}