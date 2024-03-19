package net.wirelabs.jmaps.example.components;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.example.gpx.GPXParser;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.io.File;

import java.net.URL;
import java.util.List;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class ConfigPanel extends TitledPanel {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));

    private final JButton btnAddLayer = new JButton("Load custom map");
    private final JButton btnLoadGPX = new JButton("Load gpx track");
    private final JCheckBox devMode = new JCheckBox("Tile debugger");
    private final JLabel label = new JLabel("Example map definitions");
    private final JComboBox<ExampleMap> exampleMapCombo = new JComboBox<>(ExampleMap.values());

    private final MapViewer mapViewer;
    private final transient RoutePainter routePainter;

    private JFileChooser fileChooser;

    /**
     * Configuration panel
     */
    public ConfigPanel(MapPanel mapPanel) {

        super("Config");

        this.mapViewer = mapPanel.getMapViewer();
        this.routePainter = mapPanel.getRoutePainter();

        add(label, "cell 0 1, growx");
        add(exampleMapCombo, "cell 0 2, growx");
        add(btnAddLayer, "cell 0 3, growx");
        add(btnLoadGPX, "cell 0 4, growx");
        add(devMode, "flowx,cell 0 5");

        devMode.setSelected(mapViewer.isDeveloperMode());

        setTooltips();
        setComboBoxRenderer();
        addListeners();
    }

    private void setTooltips() {
        devMode.setToolTipText("Enables visual tile debug");
        btnAddLayer.setToolTipText("Load custom map (from XML file)");
        btnLoadGPX.setToolTipText("Load and visualise a gpx track");
    }

    private void addListeners() {
        setAddMapListener();
        setLoadGPXListener();
        setDevModeListener();
        setComboChangeListener();
    }

    private void setComboBoxRenderer() {
        exampleMapCombo.setRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                ExampleMap item = (ExampleMap) value;
                setText(item.getName());
                return this;
            }
        });
    }

    private void setComboChangeListener() {

        exampleMapCombo.addActionListener(e -> {
            ExampleMap selected = (ExampleMap) exampleMapCombo.getSelectedItem();

            if (selected != null) {
                URL path = getClass().getClassLoader().getResource(selected.getMapFile());
                if (path != null) {
                    File file = new File(path.getFile());
                    mapViewer.setMap(file);
                }
            }
        });
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
                    mapViewer.setBestFit(gpxCoordinates);

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
        fileChooser.setCurrentDirectory(HOME_DIR);
        fileChooser.setFileFilter(new FileNameExtensionFilter(type, fileExtensions));
        int result = fileChooser.showOpenDialog(mapViewer);

        if (result == JFileChooser.APPROVE_OPTION) {
            actionOnApprove.run();
        }
    }

    // choose first available map
    public void setFirstAvailableMap() {
        exampleMapCombo.setSelectedIndex(0);
    }

    @Override
    protected LayoutManager customLayout() {

        return new MigLayout(
                "",
                "[grow]",
                "[][][][][][][][][][][][]");
    }
}


