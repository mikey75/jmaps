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
        // override defaults
        setDeveloperMode(true);
        setZoom(12);
        showCoordinates();
        //setRouteColor(Color.BLUE);
        //setTilerThreads(32);
        //setImageCacheSize(16000);
        setLocalCache(new DirectoryBasedCache());






        //setLayout(new MigLayout("", "[grow]", "[grow]"));
        //add(mapViewer, "cell 0 0, grow");

    }

    @Override
    protected Painter<MapViewer> createAttributionPainter() {
        MapAttributionPainter attributionPainter = new MapAttributionPainter();
        attributionPainter.setBackgroundColor(Color.YELLOW);
        attributionPainter.setPosition(SwingConstants.TOP,SwingConstants.RIGHT);
        return attributionPainter;
    }
}
