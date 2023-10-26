package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.utils.TileDebugger;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.Painter;
import net.wirelabs.jmaps.map.downloader.TileDownloader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 6/7/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Class responsible for rendering one map 'frame' of tiles on the JPanel's Graphics Context
 */
@Slf4j
public class MapRenderer {

    private final MapViewer mapViewer;
    private final TileDownloader tileDownloader;

    @Getter
    private final List<Painter<MapViewer>> painters = new ArrayList<>();
    private final Color fillColor = new Color(0, 0, 0, 0);
    private BufferedImage finalImage;
    private Graphics2D finalImageG2D;

    public MapRenderer(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
        this.tileDownloader = new TileDownloader(mapViewer);
    }

    public void renderMap(Graphics graphicsContext) {
        if (mapViewer.isMultilayer()) {
            mapViewer.setZoom(mapViewer.getZoom());
            mapViewer.setHomePosition(mapViewer.getHome());
            drawTiles(graphicsContext, mapViewer.getZoom(), mapViewer.getTopLeftCornerPoint());
            runPainters((Graphics2D) graphicsContext);
        }
    }

    private void drawTiles(final Graphics g, final int zoom, Point topLeftCorner) {

        int tileSize = mapViewer.getBaseLayer().getTileSize();

        createOutputCanvasForMultilayerTiles(tileSize);

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
                Rectangle currentTileBounds = new Rectangle(tileX * tileSize - topLeftCorner.x, tileY * tileSize - topLeftCorner.y, tileSize, tileSize);
                // only proceed if the specified tile point lies within the area being painted
                if (g.getClipBounds().intersects(currentTileBounds)) {
                    renderLayers(g, zoom, tileX, tileY, px, py);
                    if (mapViewer.isDeveloperMode()) {
                        TileDebugger.drawTileDebugInfo(g, tileSize, tileX, tileY, px, py, zoom);
                    }

                }
            }
        }


    }

    private void createOutputCanvasForMultilayerTiles(int tileSize) {
        if (mapViewer.getLayers().size() > 1 && (finalImage == null || finalImage.getHeight() != tileSize || finalImage.getWidth() != tileSize)) {
                log.info("Create finalimage");
                finalImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
                finalImageG2D = finalImage.createGraphics();
        }
    }

    private void renderLayers(Graphics g,  int zoom, int tileX, int tileY, int px, int py) {

        Layer baselayer = mapViewer.getBaseLayer();
        List<Layer> layers = mapViewer.getLayers();

        if (layers.size() == 1) {
            // render single layer map
            String tileUrl = baselayer.createTileUrl(tileX, tileY, zoom);
            BufferedImage image = tileDownloader.getTile(tileUrl);
            // only draw if it is already in cache (went throuh download)
            if (tileDownloader.isTileInCache(tileUrl)) {
                g.drawImage(image, px, py, null);
            }
        } else {

            // render multiple layers map
            finalImageG2D.setBackground(fillColor);
            finalImageG2D.clearRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

            for (Layer layer : layers) {
                AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
                String tileUrl = layer.createTileUrl(tileX, tileY, zoom + layer.getZoomOffset());
                BufferedImage b = tileDownloader.getTile(tileUrl);
                if (tileDownloader.isTileInCache(tileUrl)) {
                    if (layer.getOpacity() < 1.0f) {
                        finalImageG2D.setComposite(ac);
                    }
                    finalImageG2D.drawImage(b, 0, 0, null);
                }
            }
            // draw final image on g
            g.drawImage(finalImage, px, py, null);
        }
    }

    private void runPainters(Graphics2D graphics) {
        for (Painter<MapViewer> painter : painters) {
            painter.doPaint(graphics, mapViewer, mapViewer.getWidth(), mapViewer.getHeight());
        }
    }

    public void addPainter(Painter<MapViewer> painter) {
        painters.add(painter);
    }

    public void setLocalCache(Cache<String, BufferedImage> cache) {
        tileDownloader.setLocalCache(cache);
    }

    public void shutdownTileDownloader() {
        tileDownloader.shutdown();
    }
}
