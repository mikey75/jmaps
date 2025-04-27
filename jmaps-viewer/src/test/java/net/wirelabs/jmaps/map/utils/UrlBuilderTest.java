package net.wirelabs.jmaps.map.utils;

import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

    @Test
    void testUrlBuild() {
        UrlBuilder urlBuilder = new UrlBuilder();
        String url = "http://www.geoportal.gov.pl";
        String layerName = "topo";
        String tmsName = "EPSG:3128";
        int x = 100;
        int y = 200;

        String c = urlBuilder.parse(url)
                .addQueryParam("Service", "WMTS")
                .addQueryParam("Request", "GetTile")
                .addQueryParam("Layer", layerName)
                .addQueryParam("Version", "1.0.0")
                .addQueryParam("format", "image/png")
                .addQueryParam("style", "default")
                .addQueryParam("TileMatrixSet", tmsName)
                .addQueryParam("TileRow", String.valueOf(y))
                .addQueryParam("TileCol", String.valueOf(x))
                .build();

        assertThat(c).isEqualTo("http://www.geoportal.gov.pl?Service=WMTS&Request=GetTile&Layer=topo&Version=1.0.0&format=image%2Fpng&style=default&TileMatrixSet=EPSG%3A3128&TileRow=200&TileCol=100");
    }

}