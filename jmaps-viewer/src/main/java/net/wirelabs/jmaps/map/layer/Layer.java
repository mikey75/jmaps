package net.wirelabs.jmaps.map.layer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;


import java.awt.*;
import java.awt.geom.Point2D;


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

    protected Layer(LayerDefinition layerDefinition) {
        this.name = layerDefinition.getName();
        this.url = layerDefinition.getUrl();
        this.type = layerDefinition.getType();
        this.crs = layerDefinition.getCrs();

        this.tileSize = layerDefinition.getTileSize();
        this.maxZoom = layerDefinition.getMaxZoom();
        this.minZoom = layerDefinition.getMinZoom();
        this.opacity = layerDefinition.getOpacity();
        this.zoomOffset = layerDefinition.getZoomOffset();
        this.swapAxis = layerDefinition.isSwapAxis();

        setProjection(crs);
    }

    protected void setProjection(String crs) {
        try {
            projectionEngine.setCrs(crs);
        } catch (Exception e) {
            // if crs is null or not supported set default
            projectionEngine.setCrs("EPSG:3857");
            this.crs = "EPSG:3857";
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
        Coordinate coord = getProjectionEngine().project(latLon);
        Point2D tlc = getTopLeftCornerInMeters();

        double longitude = (coord.getLongitude() - tlc.getX()) / getMetersPerPixelAtZoom(zoom);
        double lattitude = (tlc.getY() - coord.getLatitude()) / getMetersPerPixelAtZoom(zoom);

        return new Point2D.Double(longitude, lattitude);
    }


    /**
     * Convert pixel to lat/lon
     * @param pixel current pixel
     * @param zoom zoomlevel
     * @return lat/lon coordinate
     */
    public Coordinate pixelToLatLon(Point2D pixel, int zoom) {
        Point2D tlc = getTopLeftCornerInMeters();
        // convert pixel to meters
        Coordinate coord = new Coordinate(
                tlc.getX() + (pixel.getX() * getMetersPerPixelAtZoom(zoom)),
                tlc.getY() - (pixel.getY() * getMetersPerPixelAtZoom(zoom)));
        // project back to WGS84
        return getProjectionEngine().unproject(coord);
    }

    /**
     * Get meters per pixel at given zoom
     * @param zoom zoom level
     * @return meters per pixel
     */
    public abstract double getMetersPerPixelAtZoom(int zoom);
    /**
     * Gets top left corner of the map in map's crs units
     * @return point with top left corner coordinates
     */
    public abstract Point2D getTopLeftCornerInMeters();

    /**
     * Creates download url from parameters for given layer type
     * @param x tile x
     * @param y tile y
     * @param zoom zoomlevel
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