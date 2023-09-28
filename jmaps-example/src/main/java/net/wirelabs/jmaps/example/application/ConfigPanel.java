package net.wirelabs.jmaps.example.application;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.example.GPXParser;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.List;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class ConfigPanel extends JPanel {

    private final JButton btnAddLayer = new JButton("Add Map");
    private final JButton btnLoadGPX = new JButton("Load gpx");

    private final JCheckBox devMode = new JCheckBox("Developer mode");


    /**
     * Create the panel.
     */
    public ConfigPanel(MapViewer mapViewer) {


        setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][][]"));


        add(btnAddLayer, "cell 0 1,growx");
        add(btnLoadGPX, "cell 0 2, growx");

        add(devMode, "flowx,cell 0 3");

        devMode.setSelected(MapViewer.developerMode);

        setAddMapListener(mapViewer);
        setLoadGPXListener(mapViewer);
        setDevModeListener(mapViewer);

    }


    private void setDevModeListener(MapViewer mapViewer) {
        devMode.addActionListener(e -> {
            MapViewer.developerMode = devMode.isSelected();
            mapViewer.repaint();
        });
    }
    
    private void setLoadGPXListener(MapViewer mapViewer) {
    
        btnLoadGPX.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Map definition files", "gpx"));
            int result = fileChooser.showOpenDialog(mapViewer);

            if (result == JFileChooser.APPROVE_OPTION) {
                File gpx = fileChooser.getSelectedFile();// user selects a file
                GPXParser p = new GPXParser();
                List<Coordinate> gpxCoordinates = p.parseToGeoPosition(gpx);
                mapViewer.setRoute(gpxCoordinates);
            }
        });
    }
    
    private void setAddMapListener(MapViewer mapViewer) {
        btnAddLayer.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Map definition files", "xml"));
            int result = fileChooser.showOpenDialog(mapViewer);

            if (result == JFileChooser.APPROVE_OPTION) {
                File mapxml = fileChooser.getSelectedFile();// user selects a file
                mapViewer.setMap(mapxml);
            }

        });
    }

}


