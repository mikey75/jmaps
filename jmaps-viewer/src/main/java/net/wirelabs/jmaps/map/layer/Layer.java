package net.wirelabs.jmaps.map.layer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;
import net.wirelabs.jmaps.model.map.LayerDocument;

import java.awt.*;
import java.awt.geom.*;


@Slf4j
@Getter
@Setter
public abstract class Layer  {

    protected final String name;
    protected final String url;
    protected LayerType type;
    protected String crs;
    protected int tileSize;
    protected int maxZoom;
    protected int minZoom;
    protected float opacity;
    protected int zoomOffset;
    protected boolean swapAxis;

    protected boolean enabled = true;

    private final ProjectionEngine projectionEngine = new ProjectionEngine();

    protected Layer(LayerDocument.Layer layerDefinition) {
        this.name = layerDefinition.getName();
        this.url = layerDefinition.getUrl();
        this.type = LayerType.valueOf(layerDefinition.getType());
        this.crs = layerDefinition.getCrs();

        this.tileSize = (layerDefinition.getTileSize() == 0) ? LayerDefaults.TILE_SIZE : layerDefinition.getTileSize() ;
        this.maxZoom = (layerDefinition.getMaxZoom() == 0) ? LayerDefaults.MAX_ZOOM : layerDefinition.getMaxZoom();
        this.minZoom = (layerDefinition.getMinZoom() == 0) ? LayerDefaults.MIN_ZOOM : layerDefinition.getMinZoom();
        this.opacity = (layerDefinition.getOpacity() == 0) ? LayerDefaults.OPACITY : layerDefinition.getOpacity();
        this.zoomOffset = (layerDefinition.getZoomOffset() == 0) ? LayerDefaults.ZOOM_OFFSET : layerDefinition.getZoomOffset();
        this.swapAxis = (!layerDefinition.getSwapAxis()) ? LayerDefaults.SWAP_AXIS : layerDefinition.getSwapAxis();

        setProjection(crs);
    }

    protected void setProjection(String crs) {
        try {
            projectionEngine.setCrs(crs);
        } catch (Exception e) {
            // if crs is null or not supported set default
            projectionEngine.setCrs(LayerDefaults.CRS);
            this.crs = LayerDefaults.CRS;
        }

    }

    /**
     * Convert lat/lon to pixel in layer's current CRS
     * @param latLon lat/lon coordinates
     * @param zoom zoom level
     * @return pixel point of the given lat/lon
     */
    public Point2D latLonToPixel(Coordinate latLon, int zoom) {
        // project WGS84 to crs
        Coordinate coordinate = getProjectionEngine().project(latLon);
        Point2D tlc = getTopLeftCornerInMeters();

        double longitude = (coordinate.getLongitude() - tlc.getX()) / getMetersPerPixelAtZoom(zoom);
        double latitude = (tlc.getY() - coordinate.getLatitude()) / getMetersPerPixelAtZoom(zoom);

        return new Point2D.Double(longitude, latitude);
    }


    /**
     * Convert pixel to lat/lon in layer's current CRS
     * @param pixel current pixel
     * @param zoom zoom level
     * @return lat/lon coordinate
     */
    public Coordinate pixelToLatLon(Point2D pixel, int zoom) {
        Point2D tlc = getTopLeftCornerInMeters();
        // convert pixel to crs units
        Coordinate coordinate = new Coordinate(
                tlc.getX() + (pixel.getX() * getMetersPerPixelAtZoom(zoom)),
                tlc.getY() - (pixel.getY() * getMetersPerPixelAtZoom(zoom)));
        // project back to WGS84
        return getProjectionEngine().unproject(coordinate);
    }

    /**
     * Gets top left corner of the map in map's crs units
     * @return point with top left corner coordinates
     */
    // get top left corner in meters
    public Point2D getTopLeftCornerInMeters() {

        // for default EPSG-3857 it is (-180,85.06)
        // which translates to -(equator length / 2), (polar length / 2)

        double pl = getProjectionEngine().getPolarLength() / 2.0d;
        double eq = -getProjectionEngine().getEquatorLength() / 2.0d;
        Coordinate c = new Coordinate(eq, pl); // upper left
        return new Point2D.Double(c.getLongitude(), c.getLatitude());
    }
    /**
     * Get meters per pixel at given zoom
     * @param zoom zoom level
     * @return meters per pixel
     */
    public abstract double getMetersPerPixelAtZoom(int zoom);


    /**
     * Creates download url from parameters for given layer type
     * @param x tile x
     * @param y tile y
     * @param zoom zoom level
     * @return string containing complete url ready to use
     */
    public abstract String createTileUrl(int x, int y, int zoom);
    /**
     * Returns map size in tiles at given zoom
     * @param zoom zoom level
     * @return map size
     */
    public abstract Dimension getSizeInTiles(int zoom);
}