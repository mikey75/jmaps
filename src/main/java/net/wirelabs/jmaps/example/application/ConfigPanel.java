package net.wirelabs.jmaps.example.application;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.example.GPXParser;
import net.wirelabs.jmaps.viewer.MapViewer;
import net.wirelabs.jmaps.viewer.geo.Coordinate;
import net.wirelabs.jmaps.viewer.map.MapDefinition;
import net.wirelabs.jmaps.viewer.map.painters.Painter;
import net.wirelabs.jmaps.viewer.map.painters.RoutePainter;
import net.wirelabs.jmaps.viewer.utils.MapXMLReader;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class ConfigPanel extends JPanel {

    private final JButton btnAddLayer = new JButton("Add Map");
    private final JButton btnLoadGPX = new JButton("Load gpx");

    private final JCheckBox debugTiles = new JCheckBox("debugTiles");
    private final JCheckBox chckbxShowPosition = new JCheckBox("show position");

    /**
     * Create the panel.
     */
    public ConfigPanel(MapViewer mapViewer) {


        setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][][]"));


        add(btnAddLayer, "cell 0 1,growx");
        add(btnLoadGPX, "cell 0 2, growx");

        add(debugTiles, "flowx,cell 0 3");
        add(chckbxShowPosition, "cell 0 4");

        debugTiles.setSelected(mapViewer.isTileDebug());
        chckbxShowPosition.setSelected(mapViewer.isShowCoordinates());


        setAddMapListener(mapViewer);
        setLoadGPXListener(mapViewer);
        setTileDebugListener(mapViewer);
        setShowCoordinatesListener(mapViewer);
    }

    private void setShowCoordinatesListener(MapViewer mapViewer) {
        chckbxShowPosition.addActionListener(e -> {
            mapViewer.setShowCoordinates(chckbxShowPosition.isSelected());
            mapViewer.repaint();
        });
    }

    private void setTileDebugListener(MapViewer mapViewer) {
        debugTiles.addActionListener(e -> {
            mapViewer.setTileDebug(debugTiles.isSelected());
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


