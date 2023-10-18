package net.wirelabs.jmaps.map;

import lombok.Getter;
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

        Layer baseLayer = mapViewer.getBaseLayer();
        List<Layer> layers = mapViewer.getLayers();

        int tileSize = baseLayer.getTileSize();
        int width = mapViewer.getWidth();
        int height = mapViewer.getHeight();

        // for layered final image create only when not already created or differs in size
        if (finalImage == null || finalImage.getHeight() != tileSize || finalImage.getWidth() != tileSize) {
            finalImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            finalImageG2D = finalImage.createGraphics();
        }

        // calculate the "visible" viewport area in tiles
        int numWide = width / tileSize + 2;
        int numHigh = height / tileSize + 2;


        // number of tiles in x direction
        int tpx = (int) Math.floor(topLeftCorner.getX() / tileSize);
        // number of tiles in y direction
        int tpy = (int) Math.floor(topLeftCorner.getY() / tileSize);

        // fetch the tiles from the factory and store them in the tiles cache
        // attach the tileLoadListener
        for (int x = 0; x <= numWide; x++) {
            for (int y = 0; y <= numHigh; y++) {
                int itpx = x + tpx;
                int itpy = y + tpy;

                // only proceed if the specified tile point lies within the area being painted
                if (g.getClipBounds().intersects(
                        new Rectangle(itpx * tileSize - topLeftCorner.x, itpy * tileSize - topLeftCorner.y, tileSize, tileSize))) {
                    // BASE layer
                    int ox = (itpx * tileSize - topLeftCorner.x);
                    int oy = (itpy * tileSize - topLeftCorner.y);


                    // if single layer, draw normal image onto g
                    if (layers.size() == 1) {
                        drawSingleLayerTile(g, zoom, itpx, itpy, ox, oy, baseLayer);
                    } else {
                        // multiple layers - make one image from many layers with alpha channel
                        drawMultipleLayerTile(g, zoom, finalImage, itpx, itpy, ox, oy, layers);
                    }


                if (mapViewer.isDeveloperMode()) {
                    TileDebugger.drawTileDebugInfo(g, tileSize, itpx, itpy, ox, oy, zoom);
                }



            }
        }
    }


}

    private void drawMultipleLayerTile(Graphics g, int zoom, BufferedImage finalImage, int itpx, int itpy, int ox, int oy, List<Layer> layers) {

        finalImageG2D.setBackground(fillColor);
        finalImageG2D.clearRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

        for (Layer layer: layers) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
            String tileUrl = layer.createTileUrl(itpx, itpy, zoom +layer.getZoomOffset());
            BufferedImage b = tileDownloader.getTile(tileUrl);
            if (tileDownloader.isTileInCache(tileUrl)) {
                if (layer.getOpacity() < 1.0f) {
                    finalImageG2D.setComposite(ac);
                }
                finalImageG2D.drawImage( b, 0, 0, null);
            }
        }
        // draw final image on g
        g.drawImage(finalImage, ox, oy, null);
    }

    private void drawSingleLayerTile(Graphics g, int zoom, int itpx, int itpy, int ox, int oy, Layer baseLayer) {
        String tileUrl = baseLayer.createTileUrl(itpx, itpy, zoom);
        BufferedImage b = tileDownloader.getTile(tileUrl);
        // only draw if it is already in cache (went throuh download)
        if (tileDownloader.isTileInCache(tileUrl)) {
            g.drawImage(b, ox, oy, null);
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
