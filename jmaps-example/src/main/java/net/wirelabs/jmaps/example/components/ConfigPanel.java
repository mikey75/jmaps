package net.wirelabs.jmaps.example.components;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.example.gpx.GPXParser;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.LayoutManager;
import java.io.File;
import java.util.List;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class ConfigPanel extends JPanel {

    private final JButton btnAddLayer = new JButton("Change map");
    private final JButton btnLoadGPX = new JButton("Load gpx");
    private final JCheckBox devMode = new JCheckBox("Developer mode");

    private final MapViewer mapViewer;
    private final RoutePainter routePainter;

    private JFileChooser fileChooser;

    /**
     * Configuration panel
     */
    public ConfigPanel(MapPanel mapPanel) {

        this.mapViewer = mapPanel.getMapViewer();
        this.routePainter = mapPanel.getRoutePainter();

        LayoutManager configPanelLayout = new MigLayout(
                "",
                "[grow]",
                "[][][][][][][][][][][][]"
        );

        setLayout(configPanelLayout);

        add(btnAddLayer, "cell 0 1, growx");
        add(btnLoadGPX, "cell 0 2, growx");
        add(devMode, "flowx,cell 0 3");

        devMode.setSelected(mapViewer.isDeveloperMode());

        setTooltips();
        addListeners();
    }

    private void setTooltips() {
        devMode.setToolTipText("Enables visual tile debug + cache debug logging");
        btnAddLayer.setToolTipText("Change current map source");
        btnLoadGPX.setToolTipText("Load and visualise a gpx track");
    }

    private void addListeners() {
        setAddMapListener();
        setLoadGPXListener();
        setDevModeListener();
    }

    private void setDevModeListener() {
        devMode.addActionListener(e -> {
            mapViewer.setDeveloperMode(devMode.isSelected());
            mapViewer.repaint();
        });
    }

    private void setLoadGPXListener() {

        btnLoadGPX.addActionListener(e ->
                invokeFileChooser("GPX tracks", "gpx", () -> {
                    File gpx = fileChooser.getSelectedFile();// user selects a file
                    GPXParser p = new GPXParser();
                    List<Coordinate> gpxCoordinates = p.parseToGeoPosition(gpx);
                    routePainter.setRoute(gpxCoordinates);
                }));
    }

    private void setAddMapListener() {
        btnAddLayer.addActionListener(e ->
                invokeFileChooser("Map definition files", "xml", () -> {
                    File mapxml = fileChooser.getSelectedFile();// user selects a file
                    mapViewer.setMap(mapxml);
                }));
    }

    private void invokeFileChooser(String type, String fileExtensions, Runnable actionOnApprove) {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(new FileNameExtensionFilter(type, fileExtensions));
        int result = fileChooser.showOpenDialog(mapViewer);

        if (result == JFileChooser.APPROVE_OPTION) {
            actionOnApprove.run();
        }
    }

}


