package net.wirelabs.jmaps.viewer.map;

import lombok.Getter;
import net.wirelabs.jmaps.viewer.MapViewer;
import net.wirelabs.jmaps.viewer.map.layer.Layer;
import net.wirelabs.jmaps.viewer.map.painters.Painter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
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
    private final Font font = new Font("Dialog", Font.BOLD, 10);

    public MapRenderer(MapViewer mapViewer) {
        this.mapViewer = mapViewer;

    }

    public void drawTiles(final Graphics g, final int zoom, Point topLeftCorner) {


        int tileSize = mapViewer.getMapManager().getBaseLayer().getTileSize();
        int width = mapViewer.getWidth();
        int height = mapViewer.getHeight();

        List<Layer> layers = mapViewer.getMapManager().getLayers();
        Layer baseLayer = mapViewer.getMapManager().getBaseLayer();

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


                if (mapViewer.isTileDebug()) {
                    drawTileDebugInfo(g, tileSize, itpx, itpy, ox, oy, zoom);
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
            BufferedImage b = layer.getTileProvider().getTile(tileUrl);
            if (layer.getTileProvider().isTileInCache(tileUrl)) {
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
        BufferedImage b = baseLayer.getTileProvider().getTile(tileUrl);
        // only draw if it is already in cache (went throuh download)
        if (baseLayer.getTileProvider().isTileInCache(tileUrl)) {
            g.drawImage(b, ox, oy, null);
        }
    }

    private void drawTileDebugInfo(Graphics g, int tileSize, int itpx, int itpy, int ox, int oy, int zoom) {

        g.setFont(font);
        FontMetrics fontMetrics = g.getFontMetrics();

        String text = itpx + "/" + itpy + "/" + zoom;
        Rectangle2D textBounds = fontMetrics.getStringBounds(text, g);
        // ramka i tlo na debug info
        g.setColor(Color.WHITE);
        g.fillRect(ox, oy, (int) (textBounds.getWidth() + 5), (int) (textBounds.getHeight() + 2));
        g.setColor(Color.BLACK);
        g.drawRect(ox, oy, (int) (textBounds.getWidth() + 5), (int) (textBounds.getHeight() + 2));

        // draw ramki na caly kafel
        g.drawRect(ox, oy, tileSize, tileSize);
        g.drawString(text, (ox + 2), (int) (oy + textBounds.getHeight()));
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
