package net.wirelabs.jmaps.map;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.downloader.DownloadingTileProvider;
import net.wirelabs.jmaps.map.downloader.TileProvider;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.GeoUtils;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MapViewer extends JPanel {

    private final transient MapRenderer mapRenderer;
    @Getter
    private final transient MouseHandler mouseHandler;
    private final transient TileProvider downloadingTileProvider;
    private final transient MapCreator mapCreator;

    // current map top left corner in pixels
    @Getter
    private final Point topLeftCornerPoint = new Point();
    private final MapInfoPanel mapInfoPanel;
    @Getter
    private final transient ConcurrentLinkedHashMap<String, BufferedImage> primaryTileCache;
    @Getter @Setter
    private transient Cache<String, BufferedImage> secondaryTileCache;
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
    @Getter
    private transient MapObject currentMap = new MapObject();

    public MapViewer() {
        downloadingTileProvider = new DownloadingTileProvider(this);
        mapRenderer = new MapRenderer(this, downloadingTileProvider);
        mouseHandler = new MouseHandler(this);
        mapInfoPanel = new MapInfoPanel(this);
        mapCreator = new MapCreator();

        primaryTileCache = new ConcurrentLinkedHashMap.Builder<String, BufferedImage>()
                .maximumWeightedCapacity(Defaults.DEFAULT_IMGCACHE_SIZE)
                .build();

        setLayout(new MigLayout("", "[90%][]", "[]"));
        add(mapInfoPanel, "cell 1 1, grow");
        // add listener to recenter map on map resize
        addComponentListener(new RecenterMapListener(this));
    }

    // the method that does the actual painting
    @Override
    protected void paintComponent(Graphics graphicsContext) {
        super.paintComponent(graphicsContext);
        mapRenderer.renderMap(graphicsContext);
        super.paintBorder(graphicsContext);
    }

    /**
     * Sets location on the map at current zoom and centers on it
     * If location is null or does not match map bounds then center on map's geometric center point
     *
     * @param location WGS84 coordinates of the location
     */
    public void centerOnLocation(Coordinate location) {
        // if location is NULL or outside map bounds, center on map geometric centre
        Layer baseLayer = currentMap.getBaseLayer();
        Rectangle2D mapBounds  = new Rectangle2D.Double(0,0,getMapSizeInPixels(zoom).width, getMapSizeInPixels(zoom).height);

       if (location == null || !mapBounds.contains(baseLayer.latLonToPixel(location, zoom))) {

           double x = getMapSizeInPixels(zoom).width/2.0;
            double y = getMapSizeInPixels(zoom).height/2.0;
            topLeftCornerPoint.setLocation((int) (x - getWidth() / 2.0), (int) (y - getHeight() / 2.0));
        } else {
            Point2D p = baseLayer.latLonToPixel(location, zoom);
            topLeftCornerPoint.setLocation((int) (p.getX() - getWidth() / 2.0), (int) (p.getY() - getHeight() / 2.0));
        }
    }

    public Dimension getMapSizeInPixels(int zoom) {
        Layer baselayer = currentMap.getBaseLayer();
        return new Dimension (
                baselayer.getSizeInTiles(zoom).width * baselayer.getTileSize(),
                baselayer.getSizeInTiles(zoom).height * baselayer.getTileSize());

    }
    /**
     * Set current map from xml definition file
     *
     * @param xmlMapFile map xml definition
     */
    public void setCurrentMap(File xmlMapFile) {
        try {
            currentMap = mapCreator.createMap(xmlMapFile);
            // update layers panel
            updateLayersPanel();
            // center map or best fit the route/waypoints
            centerMapOrBestFit();
        } catch (CriticalMapException e) {
            JOptionPane.showMessageDialog(getParent(), e.getMessage());
        }

    }

     void centerMapOrBestFit() {
        // if any overlay has drawn something (i.e getObjects is not empty) -> fit best to those objects
        List<Coordinate> allObjects = userOverlays.stream()
                .flatMap(listContainer -> listContainer.getObjects().stream())
                .collect(Collectors.toList());

        if (!allObjects.isEmpty()) {
            setBestFit(allObjects);
        } else {
            // otherwise center map
            setPositionAndZoom(getHome(), getZoom());
        }
    }

    public void addUserOverlay(Painter<MapViewer> painter) {
        userOverlays.add(painter);
    }

    public void setPositionAndZoom(Coordinate home, int zoom) {
        setZoom(zoom);
        centerOnLocation(home);
        repaint();
    }


    /**
     * Sets current zoom level and center so that the all coordinate points given
     * fit the current screen canvas.
     *
     * @param coordinates list of coordinates (for instance a route, or set of waypoints)
     */
    public void setBestFit(List<Coordinate> coordinates) {
        if (getCurrentMap().layersPresent()) {
            for (int fitZoom = currentMap.getBaseLayer().getMaxZoom(); fitZoom > 0; fitZoom--) {
                Rectangle2D routeRec = getEnclosingRectangle(coordinates, fitZoom);
                if (routeRec.getWidth() <= getWidth() && routeRec.getHeight() <= getHeight()) {
                    setPositionAndZoom(GeoUtils.calculateCenterOfCoordinateSet(coordinates), fitZoom);
                    return;
                }
            }
        }
    }

    private Rectangle2D getEnclosingRectangle(List<Coordinate> coords, int zoom) {

        Layer baseLayer = currentMap.getBaseLayer();

        List<Coordinate> pixelCoords = coords.stream()
                .map(coord -> new Coordinate(baseLayer.latLonToPixel(coord, zoom).getX(), baseLayer.latLonToPixel(coord, zoom).getY()))
                .collect(Collectors.toList());

        Rectangle2D r2 = GeoUtils.calculateEnclosingRectangle(pixelCoords);
        // translate world pixel into canvas pixel
        r2.setRect(r2.getX() - topLeftCornerPoint.x, r2.getY() - topLeftCornerPoint.y, r2.getWidth(), r2.getHeight());
        return r2;

    }

    public void setImageCacheSize(long size) {
        primaryTileCache.setCapacity(size);
    }

    public void setZoom(int zoom) {
        if (currentMap.layersPresent()) {

            int minZoomAllLayers = currentMap.getMinZoom();
            int maxZoomAllLayers = currentMap.getMaxZoom();

            if (zoom < minZoomAllLayers) zoom = minZoomAllLayers;
            if (zoom > maxZoomAllLayers) zoom = maxZoomAllLayers;

        }
        this.zoom = zoom;
    }

    public void updateLayersPanel() {

        if (currentMap.isMultilayer()) {
            mapInfoPanel.addLayers();
        }
        mapInfoPanel.setVisible(currentMap.isMultilayer());

    }
}


