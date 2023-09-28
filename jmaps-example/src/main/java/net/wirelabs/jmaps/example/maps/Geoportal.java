package net.wirelabs.jmaps.example.maps;


import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

public class Geoportal extends MapDefinition {

    LayerDefinition layer1 = new LayerDefinition("WMTS", "GeopoprtalBase", "http://mapy.geoportal.gov.pl/wss/service/WMTS/guest/wmts/G2_MOBILE_500")
            .withMinZoom(3)
            .withMaxZoom(14)
            .withSwapAxis(true);

    public Geoportal() {
        super("Geoportal", "GUGIK PL");
        addLayer(layer1);
        setCopyright("GUGIK Geoportal Krajowy open.data.gov.pl");
    }


}
