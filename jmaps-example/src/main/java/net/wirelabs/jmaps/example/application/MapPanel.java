package net.wirelabs.jmaps.example.application;

import net.wirelabs.jmaps.MapViewer;

import java.awt.Color;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapPanel extends MapViewer {



    public MapPanel() {
        // override defaults
        setDeveloperMode(true);
        setZoom(12);
        //setRouteColor(Color.BLUE);
        setTilerThreads(32);
        setImageCacheSize(16000);
        setLocalCache(new DirectoryBasedCache());






        //setLayout(new MigLayout("", "[grow]", "[grow]"));
        //add(mapViewer, "cell 0 0, grow");

    }


}
