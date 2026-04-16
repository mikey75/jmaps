package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.layer.Layer;

import java.awt.*;
import java.util.Locale;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Prints current world coordinates of the point under cursor
 */
public class CurrentPositionPainter extends TextPrinter {

    // default painter
    public CurrentPositionPainter() {
        this.position = ScreenPosition.BOTTOM_LEFT;
        this.framed = false;
    }

    @Override
    public void print(Graphics2D graphics, MapViewer mapViewer, int width, int height) {
        // get coordinate under mouse
        Layer baseLayer = mapViewer.getCurrentMap().getBaseLayer();
        Coordinate mousePosition = baseLayer.pixelToLatLon(mapViewer.getMouseHandler().getCurrentMousePosition(), mapViewer.getZoom());
        String crs = baseLayer.getCrs();
        String text = String.format(Locale.US,"Lon: %.4f Lat: %.4f [%s]", mousePosition.getLongitude(), mousePosition.getLatitude(), crs);
        printText(graphics, width, height, text);
    }

}
