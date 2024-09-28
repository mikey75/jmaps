package net.wirelabs.jmaps.map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.geom.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TileDebugger {

    private static final Font FONT = new Font("Dialog", Font.BOLD, 10);
    private static final String TILE_INFO_FORMAT = "[%d/%d/%d]";
    private static final int MARGIN = 4;

    public static void drawTileDebugInfo(Graphics graphics, int tileSize, int tileX, int tileY, int px, int py, int zoom) {

        graphics.setFont(FONT);

        String text = String.format(TILE_INFO_FORMAT, tileX, tileY, zoom);
        Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(text, graphics);

        // draw tile info box
        graphics.setColor(Color.WHITE);
        graphics.fillRect(px, py, (int) (textBounds.getWidth() + MARGIN), (int) (textBounds.getHeight() + MARGIN));
        graphics.setColor(Color.BLACK);
        graphics.drawRect(px, py, (int) (textBounds.getWidth() + MARGIN), (int) (textBounds.getHeight() + MARGIN));
        graphics.drawString(text, px + MARGIN / 2, (int) (py + textBounds.getHeight()) + MARGIN / 2);

        // draw tile frame
        graphics.drawRect(px, py, tileSize, tileSize);
    }
}
