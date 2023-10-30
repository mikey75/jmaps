package net.wirelabs.jmaps.example.components;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.io.File;

/**
 * Created 10/29/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Example map panel that uses mapviewer.
 * Loads example map (OSM based) from map.xml file
 * Sets home position to Lublin, PL
 */
public class MapPanel extends JPanel {

    @Getter private final transient RoutePainter routePainter = new RoutePainter();
    @Getter private final MapViewer mapViewer = new MapViewer("JMapsTiler 1.0",32,16000);

    public MapPanel() {

        setBorder(new TitledBorder("Map"));
        setLayout(new MigLayout("", "[grow]", "[grow]"));

        mapViewer.setDeveloperMode(false);
        mapViewer.setShowCoordinates(true);
        mapViewer.setZoom(12);
        mapViewer.addUserOverlay(routePainter);
        mapViewer.setLocalCache(new DirectoryBasedCache());

        add(mapViewer, "cell 0 0,grow");

    }

}
