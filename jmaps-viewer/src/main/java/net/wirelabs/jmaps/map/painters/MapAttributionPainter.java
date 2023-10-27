package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.map.MapViewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

/*
 * Created 12/20/22 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapAttributionPainter implements Painter<MapViewer> {

    // defaults
    private final Font font;
    private final Color backgroundColor;
    private final Color fontColor;
    private final Position position;
    private static final int MARGIN = 5;

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    // default painter
    public MapAttributionPainter() {
        this.font = new Font("Dialog", Font.BOLD, 10);
        this.backgroundColor = Color.WHITE;
        this.fontColor = Color.BLACK;
        this.position = Position.BOTTOM_RIGHT;
    }

    // custom painter
    public MapAttributionPainter(Font font, Color backgroundColor, Color fontColor, Position position) {
        this.font = font;
        this.backgroundColor = backgroundColor;
        this.fontColor = fontColor;
        this.position = position;

    }

    @Override
    public void doPaint(Graphics2D graphics, MapViewer mapViewer, int width, int height) {

        // get attribution text
        String attributionText = mapViewer.getMapCopyrightAttribution();
        if (!attributionText.isBlank()) {
            // set font and calculate attribution text bounding box
            graphics.setFont(font);
            FontMetrics fontMetrics = graphics.getFontMetrics();
            Rectangle2D textBounds = fontMetrics.getStringBounds(attributionText, graphics);
            int attributionWidth = (int) textBounds.getWidth();
            int attributionHeight = (int) textBounds.getHeight();

            // apply position
            Point startPoint = setStartPoint(width, height, attributionWidth, attributionHeight);
            // draw container frame
            graphics.setColor(backgroundColor);
            graphics.fillRect(startPoint.x, startPoint.y, attributionWidth, attributionHeight);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(startPoint.x, startPoint.y, attributionWidth, attributionHeight);
            // paint string
            graphics.setColor(fontColor);
            graphics.drawString(attributionText, startPoint.x, startPoint.y + fontMetrics.getAscent());
        }

    }

    private Point setStartPoint(int width, int height, int attributionWidth, int attributionHeight) {

        int x = 0;
        int y = 0;

        switch (position) {

            case TOP_LEFT:

                y = MARGIN;
                x = MARGIN;
                return new Point(x, y);

            case TOP_RIGHT:

                y = MARGIN;
                x = width - attributionWidth - MARGIN;
                return new Point(x, y);

            case BOTTOM_LEFT:

                y = height - attributionHeight - MARGIN;
                x = MARGIN;
                return new Point(x, y);

            case BOTTOM_RIGHT:

                y = height - attributionHeight - MARGIN;
                x = width - attributionWidth - MARGIN;
                return new Point(x, y);

            default:
                return new Point(x, y);
        }


    }


}