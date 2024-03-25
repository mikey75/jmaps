package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;

import java.awt.Dimension;
import java.awt.geom.Point2D;

/**
 * XYZ layer, also called basic layer, slippy map,  tile layer etc
 * Most web maps use this scheme
 * <a href="https://en.wikipedia.org/wiki/Tiled_web_map">Wikipedia article</a>
 * <p>
 * Tiles are 256 × 256 pixels.
 * Tiles use the Web Mercator coordinate reference system (EPSG:3857).
 * Tiles are available between zoom levels 0 and 18.
 * Tiles are rendered in the PNG format with an alpha channel for transparency.
 * Grid is a rectangle with 2^z rows and 2^z columns, where z is the zoom level.
 * Grid uses 0,0 as the top, left corner in the grid.
 * Tiles are found at the path z/x/y.png, where z is the zoom level, and x and y are the positions in the tile grid.
 */

public class XYZLayer extends Layer {

    public XYZLayer(LayerDefinition layerDefinition) {
        super(layerDefinition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getSizeInTiles(int zoom) {
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
    public double getMetersPerPixelAtZoom(int zoom) {
        return getProjectionEngine().getEquatorLength() / getSizeInTiles(zoom).width / tileSize;
    }


}
