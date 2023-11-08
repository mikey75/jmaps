package net.wirelabs.jmaps.map.layer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;

import java.awt.Dimension;
import java.awt.geom.Point2D;


@Slf4j
@Getter
@Setter
public abstract class Layer {

    protected final String name;
    protected final String url;

    protected int tileSize = 256;
    protected int maxZoom = 18;
    protected int minZoom = 0;
    protected float opacity = 1.0f;
    protected int zoomOffset = 0;
    protected boolean swapAxis = false;
    protected boolean enabled = true;

    protected ProjectionEngine projectionEngine;

    protected Layer(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public Dimension getMapSizeInPixels(int zoom) {
        return new Dimension (
                getMapSize(zoom).width * getTileSize(),
                getMapSize(zoom).height * getTileSize());
    }

    // these must be implemented by all layers
    /**
     * Returns map size in tiles at given zoom
     * @param zoom zoom level
     * @return map size
     */
    public abstract Dimension getMapSize(int zoom);
    public abstract String createTileUrl(int x, int y, int zoom);
    public abstract Point2D getTopLeftCorner();
    public abstract Point2D latLonToPixel(Coordinate coord, int zoom);
    public abstract Coordinate pixelToLatLon(Point2D pixel, int zoom);

}