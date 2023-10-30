package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;

import java.awt.Graphics2D;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Prints current world coordinates of the point under cursor
 */
public class CurrentPositionPainter extends TextPrinter implements Painter<MapViewer> {

    // default painter
    public CurrentPositionPainter() {
        this.position = ScreenPosition.BOTTOM_LEFT;
        this.framed = false;
    }

    @Override
    public void doPaint(Graphics2D graphics, MapViewer mapViewer, int width, int height) {
        // get coordinate under mouse
        Coordinate c = mapViewer.getBaseLayer().pixelToLatLon(mapViewer.getCurrentMousePosition(), mapViewer.getZoom());
        String text = String.format("Lon: %.4f Lat: %.4f", c.getLongitude(), c.getLatitude());
        printText(graphics, width, height, text);
    }

}
