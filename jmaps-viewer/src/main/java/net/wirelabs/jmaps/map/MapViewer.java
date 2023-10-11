package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.LayerManager;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import net.wirelabs.jmaps.map.painters.CurrentPositionPainter;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.utils.MapReader;

import javax.swing.JPanel;
import javax.xml.bind.JAXBException;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class MapViewer extends JPanel {

    @Getter
    private final transient LayerManager layerManager;
    private final transient MapRenderer mapRenderer;
    @Getter
    private final transient MouseHandler mouseHandler;
    private final transient MapReader mapReader;

    // current map top left corner in pixels
    @Getter
    private final Point topLeftCornerPoint = new Point();
    @Getter
    private final int tileCacheSize;
    @Getter
    private final int tilerThreads;
    @Getter
    private final String userAgent;

    // zoom level
    @Getter
    private int zoom = 1;
    // location for map start
    @Getter
    @Setter
    private transient Coordinate home;
    // if home position was set/activated
    @Getter
    @Setter
    private boolean homePositionSet;


    @Setter
    @Getter
    private boolean developerMode = false; // developer mode enables cache debug, tile debug and position tracking

    private static final String DEFAULT_USER_AGENT = "JMaps Tiler v.1.0";
    private static final int DEFAULT_TILER_THREADS = 16;
    private static final int DEFAULT_IMGCACHE_SIZE = 8000;
    @Getter
    private String copyright = "";

    public MapViewer() {
        this(DEFAULT_USER_AGENT, DEFAULT_TILER_THREADS, DEFAULT_IMGCACHE_SIZE);
    }

    public MapViewer(String userAgent, int tilerThreads, int tileCacheSize) {
        this.userAgent = userAgent;
        this.tilerThreads = tilerThreads;
        this.tileCacheSize = tileCacheSize;

        mapReader = new MapReader();
        layerManager = new LayerManager();
        mapRenderer = new MapRenderer(this, layerManager);
        mouseHandler = new MouseHandler(this, layerManager);

        addPainter(createAttributionPainter());

    }

    protected void showCoordinates() {
        addPainter(createCurrentPositionPainter());
    }

    protected void setLocalCache(Cache<String, BufferedImage> cache) {
        mapRenderer.setLocalCache(cache);
    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        mapRenderer.renderMap(graphicsContext);
        super.paintBorder(graphicsContext);
    }

    public void setZoom(int zoom) {
        if (layerManager.layersPresent()) {
            Layer baseLayer = layerManager.getBaseLayer();
            if (zoom < baseLayer.getMinZoom()) zoom = baseLayer.getMinZoom();
            if (zoom > baseLayer.getMaxZoom()) zoom = baseLayer.getMaxZoom();
        }
        this.zoom = zoom;
    }

    // sets location on the map at current zoom
    public void setHomePosition(Coordinate location) {
        if (!isHomePositionSet()) {
            Layer baseLayer = layerManager.getBaseLayer();
            if (location != null) { // if location given center on it
                Point2D p = baseLayer.latLonToPixel(location, zoom);
                topLeftCornerPoint.translate((int) (p.getX() - getWidth() / 2d), (int) (p.getY() - getHeight() / 2d));
            } else {
                // if, not - center map on tile grid center
                double x = baseLayer.getMapSizeInPixels(zoom).width / 2.0;
                double y = baseLayer.getMapSizeInPixels(zoom).height / 2.0;
                topLeftCornerPoint.translate((int) (x - getWidth() / 2.0), (int) (y - getHeight() / 2.0));
            }
            setHomePositionSet(true);
        }
    }

    /**
     * Set visible map from xml file
     *
     * @param xmlMapFile map xml definition
     */
    public void setMap(File xmlMapFile) {
        try {
            MapDefinition map = mapReader.loadMapDefinitionFile(xmlMapFile);
            createMap(map);
            repaint();

        } catch (JAXBException ex) {
            log.info("Map not created {}", ex.getMessage(), ex);
        }
    }

    /**
     * Add new painter
     *
     * @param painter new Painter
     */
    public void addPainter(Painter<MapViewer> painter) {
        mapRenderer.addPainter(painter);
    }

    /**
     * Creates map object (layers) from map definition and sets the map definition object
     * Also sets the home position, initial zoom and map position (corner)
     *
     * @param mapDefinition map definition
     */
    private void createMap(MapDefinition mapDefinition) {

        copyright = mapDefinition.getCopyright();
        log.info("Setting map to {}", mapDefinition.getName());
        log.info("Copyright: {}", copyright);
        // there can be only one map rendered at a time
        // so remove existing if any
        layerManager.removeAllLayers();

        for (LayerDefinition layer : mapDefinition.getLayers()) {
            layerManager.createLayer(layer);
        }

        getTopLeftCornerPoint().setLocation(0, 0);
        setZoom(getZoom());
        setHomePositionSet(false);
        setHomePosition(getHome());
    }

    // configure copyright overlay painter
    // override to set your own or configure existing
    protected Painter<MapViewer> createAttributionPainter() {
        return new MapAttributionPainter();
    }

    protected Painter<MapViewer> createCurrentPositionPainter() {
        return new CurrentPositionPainter();
    }

    // call this when disposing the mapviewer, or override and write your own
    // or connect to some application close event
    protected void onExit() {
        mapRenderer.shutdownTileDownloader();
    }
}


