package net.wirelabs.jmaps.map.utils;

import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

    @Test
    void testUrlBuild() {
        String url = "http://www.geoportal.gov.pl";
        String layerName = "topo";
        String tmsName = "EPSG:3128";
        int x = 100;
        int y = 200;

        String c = new UrlBuilder().parse(url)
                .addParam("Service", "WMTS")
                .addParam("Request", "GetTile")
                .addParam("Layer", layerName)
                .addParam("Version", "1.0.0")
                .addParam("format", "image/png")
                .addParam("style", "default")
                .addParam("TileMatrixSet", tmsName)
                .addParam("TileRow", String.valueOf(y))
                .addParam("TileCol", String.valueOf(x))
                .build();

        assertThat(c).isEqualTo("http://www.geoportal.gov.pl?Service=WMTS&Request=GetTile&Layer=topo&Version=1.0.0&format=image%2Fpng&style=default&TileMatrixSet=EPSG%3A3128&TileRow=200&TileCol=100");
    }

}