package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.viewer.geo.Coordinate;
import net.wirelabs.jmaps.viewer.geo.ProjectionEngine;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import static net.wirelabs.jmaps.viewer.geo.GeoUtils.*;

/**
 * XYZ layer, also called basic layer, slippy map,  tile layer etc
 * Most web maps use this scheme
 * <a href="https://en.wikipedia.org/wiki/Tiled_web_map">Wikipedia article</a>
 * <p>
 * Tiles are 256 × 256 pixels.
 * Tiles use the Web Mercator coordinate reference system (EPSG:3857).
 * Tiles are available between zoom levels 0 and 18.
 * Tiles are rendered in the PNG format with an alpha channel for transparency.
 * Grid is a rectangle with 2*z rows and 2*z columns, where z is the zoom level.
 * Grid uses 0,0 as the top, left corner in the grid.
 * Tiles are found at the path z/x/y.png, where z is the zoom level, and x and y are the positions in the tile grid.
 */

public class XYZLayer extends Layer {
    
    public XYZLayer(String name, String url) {
        super(name, url);
        projectionEngine = new ProjectionEngine("EPSG:3857");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getMapSize(int zoom) {
        int z = (int) Math.pow(2, zoom);
        return new Dimension(z, z);
    }

    @Override
    public String createTileUrl(int x, int y, int zoom) {
        return url
                .replace("{z}", String.valueOf(zoom))
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y));

    }

    @Override
    public Point2D getTopLeftCorner() {
        Coordinate c = new Coordinate(-180, 85.06); // upper left
        return new Point2D.Double(c.longitude, c.latitude);
    }

    @Override
    public Point2D latLonToPixel(Coordinate latLon, int zoom) {

        double e = Math.sin(deg2rad(latLon.latitude));

        if (latLon.latitude > getTopLeftCorner().getY()) latLon.latitude = getTopLeftCorner().getY();
        if (latLon.latitude < -getTopLeftCorner().getY()) latLon.latitude = -getTopLeftCorner().getY();

        double lon = centerInPixels(zoom).getX() + (latLon.longitude * oneDegreeLonInPixels(zoom));
        double lat = centerInPixels(zoom).getY() + 0.5 * Math.log((1 + e) / (1 - e)) * -oneRadianLonInPixels(zoom);

        return new Point2D.Double(lon, lat);

    }

    @Override
    public Coordinate pixelToLatLon(Point2D pixel, int zoom) {

        double lon = (pixel.getX() - centerInPixels(zoom).getX()) / oneDegreeLonInPixels(zoom);
        double e1 = (pixel.getY() - centerInPixels(zoom).getY()) /  -oneRadianLonInPixels(zoom);
        double lat = (2 * Math.atan(Math.exp(e1)) - HALF_PI) / deg2rad(1);

        return new Coordinate(lon, lat);

    }


    private Point2D centerInPixels(int zoom) {
        double centerx = (getMapSize(zoom).width * getTileSize()) / 2d;
        double centery = (getMapSize(zoom).height * getTileSize()) / 2d;
        return new Point2D.Double(centerx, centery);
    }

    private double oneDegreeLonInPixels(int zoom) {
        return (getMapSize(zoom).width * getTileSize()) / 360d;
    }

    private double oneRadianLonInPixels(int zoom) {
        return (getMapSize(zoom).width * getTileSize()) / deg2rad(360d);
    }

}
