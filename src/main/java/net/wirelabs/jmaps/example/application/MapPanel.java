package net.wirelabs.jmaps.example.application;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.example.maps.OpenStreetMap;
import net.wirelabs.jmaps.viewer.MapViewer;
import net.wirelabs.jmaps.viewer.geo.Coordinate;
import net.wirelabs.jmaps.viewer.map.painters.RoutePainter;

import javax.swing.JPanel;
import java.awt.Color;
import java.io.File;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapPanel extends JPanel {

    private File map = new File("src/main/resources/map.xml");

    @Getter
    private final MapViewer mapViewer;


    public MapPanel() {
        mapViewer = new MapViewer();
        // override defaults
        mapViewer.setCacheDebug(true);
        mapViewer.setZoom(12);
        mapViewer.setThreadCount(12);
        mapViewer.setRouteColor(Color.BLUE);
        Coordinate home = new Coordinate( 22.49004,51.23264);

        mapViewer.setHome(home);
        mapViewer.setMap(map);

        setLayout(new MigLayout("", "[grow]", "[grow]"));
        add(mapViewer, "cell 0 0, grow");

    }


}
