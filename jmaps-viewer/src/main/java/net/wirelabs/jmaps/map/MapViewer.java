package net.wirelabs.jmaps;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.model.LayerDefinition;
import net.wirelabs.jmaps.map.model.MapDefinition;
import net.wirelabs.jmaps.map.LayerManager;
import net.wirelabs.jmaps.map.MapRenderer;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.utils.MapUtils;
import net.wirelabs.jmaps.utils.MapXMLReader;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class MapViewer extends JPanel {


    private final transient LayerManager layerManager;
    private final transient MapRenderer mapRenderer;
    private final transient MouseHandler mouseHandler;

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
    private String wmtsCacheDir = Paths.get(System.getProperty("user.home"), ".jmaps-cache", "wmts-cache").toString();
    //private final transient RoutePainter defaultRoutePainter = new RoutePainter();
    //private final transient MapAttributionPainter attributionPainter = new MapAttributionPainter();


    @Setter
    public static boolean developerMode = false; // developer mode enables cache debug, tile debug and position tracking
    public static String userAgent = "JMaps Tiler v.1.0";
    public static int tilerThreads = 8;
    @Getter
    private String copyright;


    public MapViewer() {
        layerManager = new LayerManager();
        mapRenderer = new MapRenderer(this, layerManager);
        mouseHandler = new MouseHandler(this, layerManager);
       // tileDownloader = new TileDownloader(this);

        // default map has these painters (map attribution painter, and route painter)
       // addPainter(attributionPainter);
       // addPainter(defaultRoutePainter);

    }


    protected void setLocalCache(Cache<String, BufferedImage> cache) {
        mapRenderer.setLocalCache(cache);
    }

    protected void setImageCacheSize(int size) {
        mapRenderer.setImageCacheSize(size);
    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);

        if (layerManager.hasLayers()) {
            setZoom(zoom);
            setHomePosition(home);
            mapRenderer.drawTiles(graphicsContext, zoom, topLeftCornerPoint);
            mapRenderer.runPainters((Graphics2D) graphicsContext);
        }

        super.paintBorder(graphicsContext);

    }

    public void setZoom(int zoom) {
        if (layerManager.hasLayers()) {
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
            MapDefinition map = MapXMLReader.parse(xmlMapFile);
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
     * Set route to be painted on map
     *
     * @param route route
     */
   /* public void setRoute(List<Coordinate> route) {

        defaultRoutePainter.clearRoute(); // clear current route if any
        defaultRoutePainter.setRoute(route);

        topLeftCornerPoint.setLocation(0, 0);
        setHomePositionSet(false);
        setHomePosition(MapUtils.calculateCenterOfCoordinateSet(route));
        repaint();

    }*/


  /*  *//**
     * Set route stroke color
     *
     * @param color color
     *//*
    public void setRouteColor(Color color) {
        defaultRoutePainter.setColor(color);
    }*/

    public synchronized void setTilerThreads(int threads) {
        log.info("Setting thrad count to {}", threads);
        tilerThreads = threads;
    }

    /**
     * Creates map (layers) from map definition and sets the map definition object
     * Also sets the home position, initial zoom and map position (corner)
     * @param mapDefinition map definition
     */
    public void createMap(MapDefinition mapDefinition) {

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
}


