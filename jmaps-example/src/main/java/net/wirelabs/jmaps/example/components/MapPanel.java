package net.wirelabs.jmaps.example.components;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;

import java.awt.*;

/**
 * Created 10/29/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Example map panel that uses mapViewer.
 * Loads example map (OSM based) from map.xml file
 * Sets home position to Lublin, PL
 */
@Getter
public class MapPanel extends TitledPanel {

    private final transient RoutePainter routePainter = new RoutePainter();
    private final MapViewer mapViewer = new MapViewer();
    private static final Coordinate LUBLIN_PL = new Coordinate(22.565628, 51.247717);

    public MapPanel() {

        super("MapViewer");

        mapViewer.setShowCoordinates(true);
        mapViewer.setZoom(12);
        mapViewer.setHome(LUBLIN_PL);
        mapViewer.setImageCacheSize(32000);
        mapViewer.addUserOverlay(routePainter);
        add(mapViewer, "cell 0 0,grow");

    }

    @Override
    protected LayoutManager customLayout() {
        return new MigLayout("", "[grow]", "[grow]");
    }
}
