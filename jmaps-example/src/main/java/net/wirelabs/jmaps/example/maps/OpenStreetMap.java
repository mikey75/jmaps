package net.wirelabs.jmaps.example.maps;


import net.wirelabs.jmaps.map.model.LayerDefinition;
import net.wirelabs.jmaps.map.model.MapDefinition;

/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class OpenStreetMap extends MapDefinition {


    LayerDefinition layer1 = new LayerDefinition("XYZ", "OSM", "http://tile.openstreetmap.org/{z}/{x}/{y}.png")
            .withMinZoom(1)
            .withMaxZoom(18);
    
    public OpenStreetMap() {
        super("OSM", "http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        addLayer(layer1);
        setCopyright("OpenStreetMap Contributors");
    }


}
