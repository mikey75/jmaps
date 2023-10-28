package net.wirelabs.jmaps.example.components;


import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;


/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapPanel extends MapViewer {

    transient RoutePainter routePainter = new RoutePainter();

    public MapPanel() {

        super("JMapsTiler 1.0",32,16000);
        setDeveloperMode(false);
        setZoom(12);
        addUserOverlay(routePainter);
        setLocalCache(new DirectoryBasedCache());
    }


}
