package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.MapViewer;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class CurrentPositionPainter implements Painter<MapViewer> {

    @Override
    public void doPaint(Graphics2D graphics, MapViewer mapViewer, int width, int height) {
        if (mapViewer.isShowCoordinates()) {
            drawFrame(graphics, mapViewer);
            drawText(graphics, mapViewer);
        }
    }


    private void drawText(Graphics2D graphics, MapViewer mapViewer) {

        Coordinate c = mapViewer.getMapManager().getBaseLayer().pixelToLatLon(mapViewer.getMouseHandler().getMousePoint(), mapViewer.getZoom());
        String lon = String.format("Lon: %6f", c.longitude);
        String lat = String.format("Lat: %6f", c.latitude);

        graphics.drawString(lon, 20, mapViewer.getHeight() - 30);
        graphics.drawString(lat, 20, mapViewer.getHeight() - 15);
    }

    private void drawFrame(Graphics graphics, MapViewer mapViewer) {

        graphics.setColor(Color.WHITE);
        graphics.fillRect(15, mapViewer.getHeight() - 40 - 5, 150, 35);

        graphics.setColor(Color.BLACK);
        graphics.drawRect(15, mapViewer.getHeight() - 40 - 5, 150, 35);
    }
}