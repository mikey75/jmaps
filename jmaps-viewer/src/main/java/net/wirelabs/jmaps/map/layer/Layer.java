package net.wirelabs.jmaps.map.layer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;

import java.awt.Dimension;
import java.awt.geom.Point2D;


@Slf4j
public abstract class Layer {

    @Getter
    protected final String name;
    @Getter
    protected final String url;

    @Getter @Setter protected int tileSize = 256;
    @Getter @Setter protected int maxZoom = 18;
    @Getter @Setter protected int minZoom = 0;
    @Getter @Setter protected float opacity = 1.0f;
    @Getter @Setter protected int zoomOffset = 0;
    @Getter @Setter protected boolean swapAxis = false;
    @Getter @Setter protected boolean enabled = true;

    @Getter @Setter protected ProjectionEngine projectionEngine;

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