package net.wirelabs.jmaps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.geom.Rectangle2D;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TileDebugger {

    private static final Font font = new Font("Dialog", Font.BOLD, 10);

    public static void drawTileDebugInfo(Graphics g, int tileSize, int itpx, int itpy, int ox, int oy, int zoom) {

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
}
