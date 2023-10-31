package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.downloader.TileDownloader;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.LayerManager;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import net.wirelabs.jmaps.map.painters.CurrentPositionPainter;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.readers.MapReader;

import javax.swing.JPanel;
import javax.xml.bind.JAXBException;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MapViewer extends JPanel {

    private final transient LayerManager layerManager = new LayerManager();

    private final transient MapRenderer mapRenderer;
    private final transient MouseHandler mouseHandler;
    private final transient TileDownloader tileDownloader;

    // current map top left corner in pixels
    @Getter
    private final Point topLeftCornerPoint = new Point();



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
    @Getter
    @Setter
    private boolean showCoordinates = false;
    @Getter
    @Setter
    private boolean showAttribution = true;
    @Getter
    private String mapCopyrightAttribution = "";
    @Getter
    private final List<Painter<MapViewer>> userOverlays = new ArrayList<>();

    public MapViewer() {
        this(Defaults.DEFAULT_USER_AGENT, Defaults.DEFAULT_TILER_THREADS, Defaults.DEFAULT_IMGCACHE_SIZE);
    }

    public MapViewer(String userAgent, int tilerThreads, int tileCacheSize) {
        tileDownloader = new TileDownloader(this, userAgent, tilerThreads, tileCacheSize);
        mapRenderer = new MapRenderer(this, tileDownloader);
        mouseHandler = new MouseHandler(this);
    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        mapRenderer.renderMap(graphicsContext);
        super.paintBorder(graphicsContext);
    }

    public void setLocalCache(Cache<String, BufferedImage> cache) {
        tileDownloader.setLocalCache(cache);
    }

    public void setZoom(int zoom) {
        if (hasLayers()) {
            Layer baseLayer = getBaseLayer();
            if (zoom < baseLayer.getMinZoom()) zoom = baseLayer.getMinZoom();
            if (zoom > baseLayer.getMaxZoom()) zoom = baseLayer.getMaxZoom();
        }
        this.zoom = zoom;
    }

    // sets location on the map at current zoom
    public void setHomePosition(Coordinate location) {
        if (!isHomePositionSet()) {
            Layer baseLayer = getBaseLayer();
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
            MapDefinition mapDefinition = MapReader.loadMapDefinitionFile(xmlMapFile);
            parseMapDefinition(mapDefinition);
            setInitialPositionAndZoom();
            repaint();

        } catch (JAXBException ex) {
            log.info("Map not created {}", ex.getMessage(), ex);
        }
    }

    public void addUserOverlay(Painter<MapViewer> painter) {
        userOverlays.add(painter);
    }

    private void parseMapDefinition(MapDefinition mapDefinition) {

        mapCopyrightAttribution = mapDefinition.getCopyright();
        log.info("Setting map to {}", mapDefinition.getName());
        log.info("Copyright: {}", mapCopyrightAttribution);
        // there can be only one map rendered at a time
        // so remove existing if any
        layerManager.removeAllLayers();

        for (LayerDefinition layer : mapDefinition.getLayers()) {
            layerManager.createLayer(layer);
        }

    }

    public void setInitialPositionAndZoom(Coordinate home, int zoom) {
        getTopLeftCornerPoint().setLocation(0, 0);
        setZoom(zoom);
        setHomePositionSet(false);
        setHomePosition(home);
    }

    public void setInitialPositionAndZoom() {
        setInitialPositionAndZoom(getHome(), getZoom());
    }

    protected Painter<MapViewer> getCoordinatePainter() {
        return new CurrentPositionPainter();
    }

    protected Painter<MapViewer> getAttributionPainter() {
        return new MapAttributionPainter();
    }

    public boolean hasLayers() {
        return layerManager.layersPresent();
    }

    public Layer getBaseLayer() {
        return layerManager.getBaseLayer();
    }

    public List<Layer> getLayers() {
        return layerManager.getLayers();
    }

    public Point2D getCurrentMousePosition() {
        return mouseHandler.getMousePoint();
    }

}


