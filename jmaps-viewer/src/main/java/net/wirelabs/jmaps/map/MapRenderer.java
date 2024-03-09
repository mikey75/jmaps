package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.downloader.TileDownloader;
import net.wirelabs.jmaps.map.painters.TextPrinter;


import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Optional;

/**
 * Created 6/7/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Class responsible for loading and rendering one map 'frame'
 * of tiles on the JPanel's Graphics Context
 */
public class MapRenderer {

    private final MapViewer mapViewer;
    private final TileDownloader tileDownloader;

    private final TextPrinter coordinatePrinter;
    private final TextPrinter mapAttributionPrinter;
    private VolatileImage tempImage;
    private Graphics2D tempImageGraphics;

    public MapRenderer(MapViewer mapViewer, TileDownloader tileDownloader) {
        this.mapViewer = mapViewer;
        this.tileDownloader = tileDownloader;
        this.coordinatePrinter = mapViewer.getCoordinatePainter();
        this.mapAttributionPrinter = mapViewer.getAttributionPainter();
    }

    public void renderMap(Graphics graphicsContext) {
        if (mapViewer.hasLayers()) {
            renderTiles(graphicsContext, mapViewer.getZoom(), mapViewer.getTopLeftCornerPoint());
            renderUserOverlays((Graphics2D) graphicsContext);
            renderDefaultOverlays((Graphics2D) graphicsContext);
        }
    }

    private void renderTiles(final Graphics g, final int zoom, Point topLeftCorner) {

        int tileSize = mapViewer.getBaseLayer().getTileSize();

        createOutputCanvas(tileSize);

        // calculate the "visible" viewport area in tiles
        int viewportTilesWidth = mapViewer.getWidth() / tileSize + 2;
        int viewportTilesHeight = mapViewer.getHeight() / tileSize + 2;

        int numTilesX = (int) Math.floor(topLeftCorner.getX() / tileSize); // number of tiles in x direction
        int numTilesY = (int) Math.floor(topLeftCorner.getY() / tileSize); // number of tiles in y direction


        for (int x = 0; x <= viewportTilesWidth; x++) {
            for (int y = 0; y <= viewportTilesHeight; y++) {
                // tile x/y in map (same as in slippy url)
                int tileX = x + numTilesX;
                int tileY = y + numTilesY;
                // pixel x,y of tile being drawn
                int px = (tileX * tileSize - topLeftCorner.x);
                int py = (tileY * tileSize - topLeftCorner.y);
                Rectangle currentTileBounds = new Rectangle(px, py, tileSize, tileSize);
                // only proceed if the specified tile point lies within the area being painted
                if (g.getClipBounds().intersects(currentTileBounds)) {
                    if (isTileLegal(tileX, tileY, zoom)) {
                        renderTile(g, zoom, tileX, tileY, px, py);
                    }
                    if (mapViewer.isDeveloperMode()) {
                        TileDebugger.drawTileDebugInfo(g, tileSize, tileX, tileY, px, py, zoom);
                    }

                }
            }
        }
    }

    private boolean isTileLegal(int tileX, int tileY, int zoom) {
        Dimension mapsize = mapViewer.getBaseLayer().getMapSize(zoom);
        return tileX >= 0 && tileY >= 0 && tileX < mapsize.width && tileY < mapsize.height;
    }

    private void createOutputCanvas(int tileSize) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

        if ((tempImage == null || tempImage.getHeight() != tileSize || tempImage.getWidth() != tileSize)) {
            tempImage = gc.createCompatibleVolatileImage(tileSize, tileSize, Transparency.TRANSLUCENT);
            tempImageGraphics = tempImage.createGraphics();
        }
    }

    private void renderTile(Graphics g, int zoom, int tileX, int tileY, int px, int py) {

        // clear temp tile canvas
        tempImageGraphics.setBackground(Defaults.EMPTY_FILL_COLOR);
        tempImageGraphics.clearRect(0, 0, tempImage.getWidth(), tempImage.getHeight());

        for (Layer layer : mapViewer.getEnabledLayers()) {

            String tileUrl = layer.createTileUrl(tileX, tileY, zoom + layer.getZoomOffset());
            Optional<BufferedImage> b = Optional.ofNullable(tileDownloader.getTile(tileUrl));
            if (b.isPresent()) {
                // if layer's opacity is < 1.0 - apply layer's opacity alpha, otherwise set alpha 1.0f
                AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(layer.getOpacity(), 1.0f));
                tempImageGraphics.setComposite(alpha);
                tempImageGraphics.drawImage(b.get(), 0, 0, null);
            }

        }
        // draw temp image (accelerated) on g
        g.drawImage(tempImage, px, py, null);

    }

    private void renderUserOverlays(Graphics2D graphics2D) {
        for (Painter<MapViewer> painter : mapViewer.getUserOverlays()) {
            painter.doPaint(graphics2D, mapViewer, mapViewer.getWidth(), mapViewer.getHeight());
        }
    }

    private void renderDefaultOverlays(Graphics2D graphics) {
        if (mapViewer.isShowCoordinates()) {
            coordinatePrinter.print(graphics, mapViewer, mapViewer.getWidth(), mapViewer.getHeight());
        }
        if (mapViewer.isShowAttribution()) {
            mapAttributionPrinter.print(graphics, mapViewer, mapViewer.getWidth(), mapViewer.getHeight());
        }
    }

}
