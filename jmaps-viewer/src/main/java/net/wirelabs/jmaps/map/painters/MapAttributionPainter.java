package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.map.MapViewer;

import java.awt.*;

/**
 * Created 12/20/22 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Prints current map attribution string, i.e. copyright and author info
 * This is required by some (if not all) online map sources
 */
public class MapAttributionPainter extends TextPrinter  {

    // default painter
    public MapAttributionPainter() {
        this.position = ScreenPosition.BOTTOM_RIGHT;
        this.framed = true;
    }

    @Override
    public void print(Graphics2D graphics, MapViewer mapViewer, int width, int height) {

        // get attribution text
        String attributionText = mapViewer.getCurrentMap().getMapCopyrightAttribution();
        if (!attributionText.isBlank()) {
            printText(graphics, width, height, attributionText);
        }
    }



}