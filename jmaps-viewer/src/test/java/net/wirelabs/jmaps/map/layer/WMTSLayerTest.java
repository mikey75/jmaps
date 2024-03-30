package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.MockHttpServer;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WMTSLayerTest {
    private String testUrl;
    private LayerDefinition wmtsLayerDefinition;
    private MockHttpServer server;

    @BeforeEach
    void before() throws IOException {

        // serve fake capabilities

        server = new MockHttpServer();
        testUrl = "http://localhost:"+ server.getPort() +"/valid1";
        wmtsLayerDefinition = new LayerDefinition();
        wmtsLayerDefinition.setType(LayerType.WMTS);
        wmtsLayerDefinition.setName("TestWmts");
        wmtsLayerDefinition.setUrl(testUrl);

    }

    @AfterEach
    void after() throws IOException {
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
        assertThat(wmts.isSwapAxis()).isFalse(); //boolean swapAxis = false;
        assertThat(wmts.getOpacity()).isEqualTo(1.0f);
        assertThat(wmts.getZoomOffset()).isZero();


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
        // example of custom wmts map

        // override defaults in capabilities.xml
        String overridenWmtsLayer = "A2";
        wmtsLayerDefinition.setWmtsLayer(overridenWmtsLayer);
        wmtsLayerDefinition.setMinZoom(3);
        wmtsLayerDefinition.setSwapAxis(true);
        wmtsLayerDefinition.setCrs("EPSG:3187");

        Layer wmts = new WMTSLayer(wmtsLayerDefinition);

        assertThat(wmts.getMinZoom()).isEqualTo(3);
        assertThat(wmts.isSwapAxis()).isTrue();
        assertThat(wmts.getCrs()).isEqualTo("EPSG:3187");

        assertThat(wmts.createTileUrl(10, 11, 15)).isEqualTo(
                HttpUrl.parse(testUrl).newBuilder()
                        .addQueryParameter("Service","WMTS")
                        .addQueryParameter("Request","GetTile")
                        .addQueryParameter("Layer",overridenWmtsLayer)
                        .addQueryParameter("Version","1.0.0")
                        .addQueryParameter("format","image/png")
                        .addQueryParameter("style","default")
                        .addQueryParameter("TileMatrixSet","EPSG:2180")
                        .addQueryParameter("TileMatrix","EPSG:2180:15")
                        .addQueryParameter("TileRow","11")
                        .addQueryParameter("TileCol","10")
                        .toString());


        // since test capabilities.xml does not have NEW_TMS tilematrixset
        // it should throws exception
        wmtsLayerDefinition.setTileMatrixSet("NEW_TMS");
        assertThatThrownBy(() -> new WMTSLayer(wmtsLayerDefinition)).hasMessage("TileMatrixSet NEW_TMS does not exist in the map");
    }

}
