package net.wirelabs.jmaps.viewer.tiler;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@Disabled
class TileDownloaderTest {

    /*@Test
    void t() throws InterruptedException {
        TileProvider td = new TileProvider();
        for (int x = 5; x < 10; x++) {
            for (int y = 5; y < 10; y++) {
                td.download("http://tile.openstreetmap.org/4/" + x + "/" + y + ".png");
                td.download("http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/G2_MOBILE_500" +
                        "?Service=WMTS&Request=GetTile&Layer=G2_MOBILE_500&TileMatrixSet=EPSG:2180" +
                        "&TileMatrix=EPSG:2180:6" +
                        "&TileRow=" + x +
                        "&TileCol=" + y);
            }

        }
        Thread.sleep(Duration.ofSeconds(5).toMillis());


        log.info("break");
    }*/
}