package net.wirelabs.jmaps.example.application;


import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;

import javax.swing.SwingConstants;
import java.awt.Color;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapPanel extends MapViewer {



    public MapPanel() {
        super("JMapsTiler 1.0",32,16000);
        setDeveloperMode(false);
        setZoom(12);
        //showCoordinates();
        //setRouteColor(Color.BLUE);
        //setTilerThreads(32);
        //setImageCacheSize(16000);
        setLocalCache(new DirectoryBasedCache());

    }


}
