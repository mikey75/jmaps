package net.wirelabs.jmaps;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import net.wirelabs.jmaps.map.MapDefinition;
import net.wirelabs.jmaps.map.MapManager;
import net.wirelabs.jmaps.map.MapRenderer;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.CurrentPositionPainter;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.painters.RoutePainter;
import net.wirelabs.jmaps.utils.MapXMLReader;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

import javax.swing.JPanel;
import javax.xml.bind.JAXBException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MapViewer extends JPanel {

    @Getter private final transient MapManager mapManager;
    @Getter private final transient MapRenderer mapRenderer;
    @Getter private final transient MouseHandler mouseHandler;

    // current map top left corner in pixels
    @Getter private final Point topLeftCornerPoint = new Point();
    // zoom level
    @Getter private int zoom = 1;
    // location for map start
    @Getter @Setter private transient Coordinate home;
    // if home position was set/activated
    @Getter @Setter private boolean homePositionSet;

    // default settings, changeable with setters before loading first map
    @Getter @Setter private boolean showCoordinates = false;
    @Getter @Setter private boolean tileDebug = false;
    @Getter @Setter private boolean cacheDebug = false;
    @Getter @Setter private boolean showCopyright = true;
    @Getter @Setter private int imageCacheSize = 8000;
    @Getter @Setter private int threadCount = 8;
    @Setter @Getter private String userAgent = "JMaps Tiler v.1.0";

    @Setter @Getter private String wmtsCacheDir = Paths.get(System.getProperty("user.home"), ".jmaps-cache","wmts-cache").toString();
    @Setter @Getter private String tileCacheDir = Paths.get(System.getProperty("user.home"), ".jmaps-cache","tile-cache").toString();

    private final transient RoutePainter defaultRoutePainter = new RoutePainter();

    public MapViewer() {
        mapRenderer = new MapRenderer(this);
        mapManager = new MapManager(this);
        mouseHandler = new MouseHandler(this);

        // default map has these painters (coordinate painter, map attribution painter, and route painter)
        MapAttributionPainter attributionPainter = new MapAttributionPainter();
        CurrentPositionPainter positionPainter = new CurrentPositionPainter();

        addPainter(attributionPainter);
        addPainter(positionPainter);
        addPainter(defaultRoutePainter);

    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);

        if (getMapManager().hasLayers()) {
            setZoom(zoom);
            setHomePosition(home);
            mapRenderer.drawTiles(graphicsContext, zoom, topLeftCornerPoint);
            mapRenderer.runPainters((Graphics2D) graphicsContext);
        }

        super.paintBorder(graphicsContext);

    }

    public void setZoom(int zoom) {
        if (getMapManager().hasLayers()) {
            Layer baseLayer = getMapManager().getBaseLayer();
            if (zoom < baseLayer.getMinZoom()) zoom = baseLayer.getMinZoom();
            if (zoom > baseLayer.getMaxZoom()) zoom = baseLayer.getMaxZoom();
        }
        this.zoom = zoom;
    }

    // sets location on the map at current zoom
    public void setHomePosition(Coordinate location) {
        if (!isHomePositionSet()) {
            Layer baseLayer = getMapManager().getBaseLayer();
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
     * Set visible map
     * @param md map definition class object
     */
    public void setMap(MapDefinition md) {
        mapManager.createMap(md);
    }

    /**
     * Set visible map from xml file
     * @param xmlMapFile map xml definition
     */
    public void setMap(File xmlMapFile) {
        try {
            MapDefinition map = MapXMLReader.parse(xmlMapFile);
            setMap(map);
            repaint();

        } catch (JAXBException ex) {
            log.info("Map not created {}", ex.getMessage(), ex);
        }
    }

    /**
     * Add new painter
     * @param painter new Painter
     */
    public void addPainter(Painter<MapViewer> painter) {
        mapRenderer.addPainter(painter);
    }

    /**
     * Set route to be painted on map
     * @param route route
     */
    public void setRoute(List<Coordinate> route) {
       // Rectangle2D bbox = calculateBBox(route);

        defaultRoutePainter.clearRoute(); // clear current route if any
        defaultRoutePainter.setRoute(route);


        topLeftCornerPoint.setLocation(0,0);
        setHomePositionSet(false);
        setHomePosition(calculateCenter(route));
        repaint();

    }

    private Coordinate calculateCenter(List<Coordinate> route) {
        log.info("Start calcylate");
        double minX =  route.stream().min(Comparator.comparing(c -> c.longitude)).get().longitude;
        double maxX =  route.stream().max(Comparator.comparing(c -> c.longitude)).get().longitude;
        double minY =  route.stream().min(Comparator.comparing(c ->c.latitude)).get().latitude;
        double maxY =  route.stream().max(Comparator.comparing(c ->c.latitude)).get().latitude;
        double cx = (maxX-minX) / 2.0;
        double cy = (maxY-minY) / 2.0;
        log.info("End calculate");
        return new Coordinate(minX+ cx, minY+cy);
    }

   /* private Rectangle2D calculateBBox(List<Coordinate> route) {
        Layer baseLayer = getMapManager().getBaseLayer();

        double minX =  route.stream().min(Comparator.comparing(c -> c.longitude)).get().longitude;
        double maxX =  route.stream().max(Comparator.comparing(c -> c.longitude)).get().longitude;
        double minY =  route.stream().min(Comparator.comparing(c ->c.latitude)).get().latitude;
        double maxY =  route.stream().max(Comparator.comparing(c ->c.latitude)).get().latitude;



        return new Rectangle2D.Double(minX,minY,maxY-minY, maxX - minX);
    }*/


    /**
     * Set route stroke color
     * @param color color
     */
    public void setRouteColor(Color color) {
        defaultRoutePainter.setColor(color);
    }
}


