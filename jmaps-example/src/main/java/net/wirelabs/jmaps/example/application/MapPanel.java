package net.wirelabs.jmaps.example.application;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import net.wirelabs.jmaps.MapViewer;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapPanel extends JPanel {



    @Getter
    private final MapViewer mapViewer;


    public MapPanel() {
        mapViewer = new MapViewer();
        // override defaults
        mapViewer.setCacheDebug(true);
        mapViewer.setZoom(12);
        mapViewer.setThreadCount(12);
        mapViewer.setRouteColor(Color.BLUE);



        setLayout(new MigLayout("", "[grow]", "[grow]"));
        add(mapViewer, "cell 0 0, grow");

    }


}
