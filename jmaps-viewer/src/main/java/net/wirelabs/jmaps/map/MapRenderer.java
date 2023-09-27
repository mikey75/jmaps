package net.wirelabs.jmaps.map;

import lombok.Getter;
import net.wirelabs.jmaps.MapViewer;
import net.wirelabs.jmaps.TileDebugger;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.painters.Painter;

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
    @Getter
    private final List<Painter<MapViewer>> painters = new ArrayList<>();


    public MapRenderer(MapViewer mapViewer) {
        this.mapViewer = mapViewer;

    }

    public void drawTiles(final Graphics g, final int zoom, Point topLeftCorner) {


        int tileSize = mapViewer.getLayerManager().getBaseLayer().getTileSize();
        int width = mapViewer.getWidth();
        int height = mapViewer.getHeight();

        List<Layer> layers = mapViewer.getLayerManager().getLayers();
        Layer baseLayer = mapViewer.getLayerManager().getBaseLayer();

        BufferedImage finalImage = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);

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


                if (MapViewer.developerMode) {
                    TileDebugger.drawTileDebugInfo(g, tileSize, itpx, itpy, ox, oy, zoom);
                }


            }
        }
    }


}

    private void drawMultipleLayerTile(Graphics g, int zoom, BufferedImage finalImage, int itpx, int itpy, int ox, int oy, List<Layer> layers) {
        Graphics2D finalImageG2D = finalImage.createGraphics();
        finalImageG2D.setBackground(new Color(0, 0, 0, 0));
        finalImageG2D.clearRect(0, 0, finalImage.getWidth(), finalImage.getHeight());

        for (Layer layer: layers) {
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, layer.getOpacity());
            String tileUrl = layer.createTileUrl(itpx, itpy, zoom +layer.getZoomOffset());
            BufferedImage b = mapViewer.getTileDownloader().getTile(tileUrl);
            if (mapViewer.getTileDownloader().isTileInCache(tileUrl)) {
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
        BufferedImage b = mapViewer.getTileDownloader().getTile(tileUrl);
        // only draw if it is already in cache (went throuh download)
        if (mapViewer.getTileDownloader().isTileInCache(tileUrl)) {
            g.drawImage(b, ox, oy, null);
        }
    }

    public void runPainters(Graphics2D graphics) {
        for (Painter<MapViewer> p : painters) {
            p.doPaint(graphics, mapViewer, mapViewer.getWidth(), mapViewer.getHeight());
        }
    }

    public void addPainter(Painter<MapViewer> painter) {
        painters.add(painter);
    }

}
