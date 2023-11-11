package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.tileprovider.TileProvider;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.GeoUtils;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import net.wirelabs.jmaps.map.painters.CurrentPositionPainter;
import net.wirelabs.jmaps.map.painters.MapAttributionPainter;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.readers.MapReader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MapViewer extends JPanel {

    private final transient MapRenderer mapRenderer;
    private final transient MouseHandler mouseHandler;
    private final transient TileProvider tileDownloader;
    private final transient MapManager mapManager;

    private final transient CurrentPositionPainter currentPositionPainter = new CurrentPositionPainter();
    private final transient MapAttributionPainter attributionPainter = new MapAttributionPainter();

    // current map top left corner in pixels
    @Getter
    private final Point topLeftCornerPoint = new Point();
    private final MapInfoPanel mapInfoPanel;
    @Getter @Setter
    private String userAgent = Defaults.DEFAULT_USER_AGENT;
    @Getter @Setter
    private int tilerThreads = Defaults.DEFAULT_TILER_THREADS;
    // zoom level
    @Getter
    private int zoom = 3;

    // location for map start
    @Getter @Setter
    private transient Coordinate home;
    @Getter @Setter
    private boolean developerMode = false; // developer mode enables cache debug, tile debug and position tracking
    @Getter @Setter
    private boolean showCoordinates = false;
    @Getter @Setter
    private boolean showAttribution = true;
    @Getter
    private final List<Painter<MapViewer>> userOverlays = new ArrayList<>();

    public MapViewer() {
        tileDownloader = new TileProvider(this);
        mapRenderer = new MapRenderer(this, tileDownloader);
        mapManager = new MapManager();
        mouseHandler = new MouseHandler(this);
        mapInfoPanel = new MapInfoPanel(this, mapManager);
        add(mapInfoPanel);
    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        mapRenderer.renderMap(graphicsContext);
        super.paintBorder(graphicsContext);
    }

    public void setLocalCache(Cache<String, BufferedImage> cache) {
        tileDownloader.setSecondaryTileCache(cache);
    }

    public void setZoom(int zoom) {
        if (hasLayers()) {

            int minZoomAllLayers = getMinZoom();
            int maxZoomAllLayers = getMaxZoom();

            if (zoom < minZoomAllLayers) zoom = minZoomAllLayers;
            if (zoom > maxZoomAllLayers) zoom = maxZoomAllLayers;

        }
        this.zoom = zoom;
    }

    public int getMaxZoom() {
        return mapManager.getMaxZoom();
    }

    public int getMinZoom() {
        return mapManager.getMinZoom();
    }

    // sets location (given in wgs84 coordinates)
    // on the map at current zoom and centers on it
    // if location is null - center on map's center point
    public void centerOnLocation(Coordinate location) {

        Layer baseLayer = getBaseLayer();
        if (location != null) { // if location given center on it
            Point2D p = baseLayer.latLonToPixel(location, zoom);
            topLeftCornerPoint.translate((int) (p.getX() - getWidth() / 2.0), (int) (p.getY() - getHeight() / 2.0));
        } else {
            // if, not - center map on tile grid center
            double x = baseLayer.getMapSizeInPixels(zoom).width / 2.0;
            double y = baseLayer.getMapSizeInPixels(zoom).height / 2.0;
            topLeftCornerPoint.translate((int) (x - getWidth() / 2.0), (int) (y - getHeight() / 2.0));
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
            mapManager.createMap(mapDefinition);
            // update layers panel
            updateLayersPanel();
            setPositionAndZoom(getHome(), getZoom());

        } catch (CriticalMapException ex) {
            log.info("Map not created {}", ex.getMessage(), ex);
        }
    }

    public void addUserOverlay(Painter<MapViewer> painter) {
        userOverlays.add(painter);
    }

    public void setPositionAndZoom(Coordinate home, int zoom) {
        getTopLeftCornerPoint().setLocation(0, 0);
        setZoom(zoom);
        centerOnLocation(home);
        repaint();
    }

    protected Painter<MapViewer> getCoordinatePainter() {
        return currentPositionPainter;
    }

    protected Painter<MapViewer> getAttributionPainter() {
        return attributionPainter;
    }

    public boolean hasLayers() {
        return mapManager.layersPresent();
    }

    public Layer getBaseLayer() {
        return mapManager.getBaseLayer();
    }

    public Point2D getCurrentMousePosition() {
        return mouseHandler.getMousePoint();
    }

    /**
     * Sets current zoom level and center so that the all coordinate points given
     * fit the current screen canvas.
     *
     * @param coordinates list of coordinates (for instance a route, or set of waypoints)
     */
    public void setBestFit(List<Coordinate> coordinates) {
        if (hasLayers()) {
            for (int fitZoom = getBaseLayer().getMaxZoom(); fitZoom > 0; fitZoom--) {
                Rectangle2D routeRec = getEnclosingRectangle(coordinates, fitZoom);
                if (routeRec.getWidth() <= getWidth() && routeRec.getHeight() <= getHeight()) {
                    setPositionAndZoom(GeoUtils.calculateCenterOfCoordinateSet(coordinates), fitZoom);
                    return;
                }
            }
        }
    }

    /**
     * Calculate enclosing rectangle such that all coordinate points fit inside it
     * The resulting rectangle is in screen pixels, not world coordinates
     * @param coords list of coordinates (for instance a route, or set of waypoints)
     * @param zoom zoom level
     * @return resulting rectangle
     */
    private Rectangle2D getEnclosingRectangle(List<Coordinate> coords, int zoom) {

        // --- setup first point rectangle
        Point2D firstPoint = getBaseLayer().latLonToPixel(coords.get(0), zoom);
        Rectangle2D r2 = new Rectangle2D.Double(firstPoint.getX(), firstPoint.getY(), 0, 0);

        // add points to rectangle
        for (Coordinate c : coords) {
            r2.add(getBaseLayer().latLonToPixel(c, zoom));
        }
        // translate world pixel into canvas pixel
        r2.setRect(r2.getX() - topLeftCornerPoint.x, r2.getY() - topLeftCornerPoint.y, r2.getWidth(), r2.getHeight());
        // draw
        return r2;

    }

    public List<Layer> getEnabledLayers() {
        return mapManager.getEnabledLayers();
    }

    public String getMapCopyrightAttribution() {
        return mapManager.getMapCopyrightAttribution();
    }

    public void setImageCacheSize(long size) {
        tileDownloader.setImageCacheSize(size);
    }

    public void updateLayersPanel() {

        if (mapManager.isMultilayer()) {
            mapInfoPanel.addLayers();
            mapInfoPanel.setVisible(true);
        } else {
            mapInfoPanel.setVisible(false);
        }
    }
}


