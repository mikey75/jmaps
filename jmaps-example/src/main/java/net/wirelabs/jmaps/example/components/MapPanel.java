package net.wirelabs.jmaps.example.components;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * Created 10/29/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Example map panel that uses mapviewer.
 * Loads example map (OSM based) from map.xml file
 * Sets home position to Lublin, PL
 */
@Getter
public class MapPanel extends JPanel {

    private final transient RoutePainter routePainter = new RoutePainter();
    private final MapViewer mapViewer = new MapViewer();

    public MapPanel() {

        setBorder(new TitledBorder("Map"));
        setLayout(new MigLayout("", "[grow]", "[grow]"));
        mapViewer.setShowCoordinates(true);
        mapViewer.setZoom(12);
        mapViewer.setImageCacheSize(32000);
        mapViewer.setLocalCache(new DirectoryBasedCache());
        mapViewer.addUserOverlay(routePainter);
        add(mapViewer, "cell 0 0,grow");

    }

}
