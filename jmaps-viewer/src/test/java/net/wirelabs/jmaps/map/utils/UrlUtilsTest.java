package net.wirelabs.jmaps.map.utils;

import net.wirelabs.jmaps.TestUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlUtilsTest {
    @Test
    void shouldParseWmtsUrl() throws IOException {
        String WMTS_URL1 = "https://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/BDOT10k" +
                "?Service=WMTS" +
                "&Request=GetTile" +
                "&Layer=BDOT10k" +
                "&Version=1.0.0" +
                "&format=image/png" +
                "&style=default" +
                "&TileMatrixSet=EPSG:2180" +
                "&TileMatrix=EPSG:2180:9" +
                "&TileRow=337" +
                "&TileCol=476";

        String result = UrlUtils.urlToStringPath(WMTS_URL1);

        assertThat(result).isEqualTo("mapy_geoportal_gov_pl/wss/service/WMTS/guest/wmts/BDOT10k" +
                "/WMTS" +
                "/GetTile" +
                "/BDOT10k" +
                "/1_0_0" +
                "/image:png" +
                "/default" +
                "/EPSG:2180" +
                "/EPSG:2180:9" +
                "/337" +
                "/476");

    }

    @Test
    void shouldParseUrlEncodedWmtsUrl() throws IOException {


        String WMTS_URL2 = "http://geoportal.cuzk.cz/WMTS_ZM/WMTService.aspx" +
                "?Service=WMTS" +
                "&Request=GetTile" +
                "&Layer=zm" +
                "&Version=1.0.0" +
                "&format=image%2Fpng" +
                "&style=default" +
                "&TileMatrixSet=wgs84%3Apseudomercator%3Aepsg%3A3857" +
                "&TileMatrix=9" +
                "&TileRow=258" +
                "&TileCol=510";

        String result = UrlUtils.urlToStringPath(WMTS_URL2);

        assertThat(result).isEqualTo(
        "geoportal_cuzk_cz/WMTS_ZM/WMTService_aspx" +
                "/WMTS" +
                "/GetTile" +
                "/zm" +
                "/1_0_0" +
                "/image:png" +
                "/default" +
                "/wgs84:pseudomercator:epsg:3857" +
                "/9" +
                "/258" +
                "/510");

    }

    @Test
    void shouldParseOrdinaryXYZurl() throws IOException {
        String result = UrlUtils.urlToStringPath("http://tile.openstreetmap.org/12/2298/1365.png");
        assertThat(result).isEqualTo("tile_openstreetmap_org/12/2298/1365_png");
    }

    @Test
    void shouldParseXYZUrlWithParams() throws IOException {
        String result = UrlUtils.urlToStringPath("https://api.mapy.cz/v1/maptiles/outdoor/256/15/18427/10935?apikey=xxxx");
        assertThat(result).isEqualTo("api_mapy_cz/v1/maptiles/outdoor/256/15/18427/10935/xxxx");
    }

    @Test
    void shouldParseTooLongUrl() {
        String url = "http://tile.openstreetmap.org/12/2298/" + TestUtils.getRandomString(300);
        assertThrows(IOException.class, () -> UrlUtils.urlToStringPath(url));
    }

    @Test
    void shouldParseQuadUrl() throws IOException {
        String result = UrlUtils.urlToStringPath("http://r1.ortho.tiles.virtualearth.net/tiles/r1203020221.png?g=1");
        assertThat(result).isEqualTo("r1_ortho_tiles_virtualearth_net/tiles/r1203020221_png/1");
    }


}