package net.wirelabs.jmaps.example.components;

import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.DBCache;
import net.wirelabs.jmaps.map.cache.DirectoryBasedCache;
import net.wirelabs.jmaps.map.geo.Coordinate;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class ConfigPanel extends TitledPanel {

    public static final File HOME_DIR = new File(System.getProperty("user.home"));

    private final JButton btnAddLayer = new JButton("Load custom map");
    private final JButton btnLoadGPX = new JButton("Load gpx track");
    private final JCheckBox devMode = new JCheckBox("Tile debugger");
    private final JLabel lblMapDefinitions = new JLabel("Example map definitions");
    private final JLabel lblCache = new JLabel("Cache to use");
    private final JComboBox<ExampleMap> exampleMapCombo = new JComboBox<>(ExampleMap.values());
    private final JComboBox<String> cacheCombo = new JComboBox<>(new String[]{"Files","Database"});

    private final MapViewer mapViewer;
    private final transient RoutePainter routePainter;
    private final String tempDir = System.getProperty("java.io.tmpdir");
    private final transient DBCache dbCache = new DBCache(Path.of(tempDir, "testdbcache"), Duration.ofDays(30));
    private final transient DirectoryBasedCache fileCache = new DirectoryBasedCache(Path.of(tempDir, "testfilecache"), Duration.ofDays(30));
    private JFileChooser fileChooser;

    /**
     * Configuration panel
     */
    public ConfigPanel(MapPanel mapPanel) {

        super("Config");

        this.mapViewer = mapPanel.getMapViewer();
        this.routePainter = mapPanel.getRoutePainter();

        add(lblMapDefinitions, "cell 0 1, growx");
        add(exampleMapCombo, "cell 0 2, growx");
        add(btnAddLayer, "cell 0 3, growx");
        add(btnLoadGPX, "cell 0 4, growx");
        add(lblCache, "cell 0 5, growx");
        add(cacheCombo, "cell 0 6, growx");
        add(devMode, "flowx,cell 0 7");

        devMode.setSelected(mapViewer.isDeveloperMode());

        setTooltips();
        setComboBoxRenderer();
        addListeners();
        cacheCombo.setSelectedIndex(0); // use files
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
        changeCacheListener();
    }

    private void changeCacheListener() {
        cacheCombo.addActionListener(e -> {
            String selectedItem = (String) Objects.requireNonNull(cacheCombo.getSelectedItem());

            if (selectedItem.equals("Database") && (!(mapViewer.getSecondaryTileCache() instanceof DBCache))) {
                mapViewer.setSecondaryTileCache(dbCache);
                log.info("Cache changed to {}", dbCache);
            }

            if (selectedItem.equals("Files") && (!(mapViewer.getSecondaryTileCache() instanceof DirectoryBasedCache))) {
                mapViewer.setSecondaryTileCache(fileCache);
                log.info("Cache changed to {}", fileCache);
            }
        });
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
                log.info("Selected map: {}", selected.getMapFile());
                    File finalFile = new File(System.getProperty("user.dir")+"/example-maps", selected.getMapFile());
                    log.info("Final map path {}", finalFile);
                    mapViewer.setCurrentMap(finalFile);

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
                    mapViewer.setCurrentMap(mapxml);
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


