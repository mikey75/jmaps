package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.model.map.LayerDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class WMTSLayerTest {
    private String testUrl;
    private LayerDocument.Layer wmtsLayerDefinition;
    private MockHttpServer server;

    @BeforeEach
    void before() throws IOException {

        // serve fake capabilities

        server = new MockHttpServer();
        testUrl = "http://localhost:"+ server.getPort() +"/valid1";
        wmtsLayerDefinition = LayerDocument.Layer.Factory.newInstance();
        wmtsLayerDefinition.setType(String.valueOf(LayerType.WMTS));
        wmtsLayerDefinition.setName("TestWmts");
        wmtsLayerDefinition.setUrl(testUrl);

    }

    @AfterEach
    void after() {
        server.stop();
    }

    @Test
    void shouldProperlyInitializeDefaultWMTSLayer() {
        // example of wmts map

        Layer wmts = new WMTSLayer(wmtsLayerDefinition);

        // all defaults from xml capabilities
        assertThat(wmts.getType()).isEqualTo(LayerType.WMTS);
        assertThat(wmts.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180"); // assert default crs, we did not set
        assertThat(wmts.getCrs()).isEqualTo("EPSG:2180");
        assertThat(wmts.getTileSize()).isEqualTo(512);
        assertThat(wmts.getMaxZoom()).isEqualTo(15);
        assertThat(wmts.getMinZoom()).isZero();
        assertThat(wmts.isSwapAxis()).isFalse();
        assertThat(wmts.getOpacity()).isEqualTo(1.0f);
        assertThat(wmts.getZoomOffset()).isZero();


        assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                testUrl + "?Service=WMTS" +
                        "&Request=GetTile" +
                        "&Layer=G2_MOBILE_500" +
                        "&Version=1.0.0" +
                        "&format=image%2Fpng" +
                        "&style=default" +
                        "&TileMatrixSet=EPSG%3A2180" +
                        "&TileMatrix=EPSG%3A2180%3A15" +
                        "&TileRow=11" +
                        "&TileCol=10");
    }


    @Test
    void shouldProperlyInitializeCustomWMTSLayer() {
        // example of custom wmts map

        // override defaults in capabilities.xml
        // tms and layer are nonexistent in xml file
        // so defaults shoule be returned
        wmtsLayerDefinition.setTileMatrixSet("NEW_TMS");
        wmtsLayerDefinition.setWmtsLayer("A2");
        // those should be overriden and returned
        wmtsLayerDefinition.setMinZoom(3);
        wmtsLayerDefinition.setSwapAxis(true);
        wmtsLayerDefinition.setCrs("EPSG:3187");

        Layer wmts = new WMTSLayer(wmtsLayerDefinition);

        assertThat(wmts.getMinZoom()).isEqualTo(3);
        assertThat(wmts.isSwapAxis()).isTrue();
        assertThat(wmts.getCrs()).isEqualTo("EPSG:3187");

        assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                testUrl + "?Service=WMTS" +
                        "&Request=GetTile" +
                        "&Layer=G2_MOBILE_500" +
                        "&Version=1.0.0" +
                        "&format=image%2Fpng" +
                        "&style=default" +
                        "&TileMatrixSet=EPSG%3A2180" +
                        "&TileMatrix=EPSG%3A2180%3A15" +
                        "&TileRow=11" +
                        "&TileCol=10"
        );


    }

}
