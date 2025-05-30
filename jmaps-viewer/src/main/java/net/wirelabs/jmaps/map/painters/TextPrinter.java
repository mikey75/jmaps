package net.wirelabs.jmaps.map.painters;

import lombok.Setter;
import net.wirelabs.jmaps.map.MapViewer;

import java.awt.*;
import java.awt.geom.*;

/**
 * Created 10/29/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Setter
public abstract class TextPrinter {

    protected ScreenPosition position;
    protected Font font = new Font("Dialog", Font.BOLD, 10);
    protected Color backgroundColor = Color.WHITE;
    protected Color fontColor = Color.BLACK;

    protected boolean framed = true;
    protected int margin = 2;

    public abstract void print(Graphics2D graphics, MapViewer mapViewer, int width, int height);

    protected Point setStartPoint(int width, int height, int textWidth, int textHeight) {

        int x;
        int y;

        switch (position) {

            case TOP_LEFT:

                y = margin;
                x = margin;
                return new Point(x, y);

            case TOP_RIGHT:

                y = margin;
                x = width - textWidth - margin;
                return new Point(x, y);

            case BOTTOM_LEFT:

                y = height - textHeight - margin;
                x = margin;
                return new Point(x, y);

            case BOTTOM_RIGHT:

                y = height - textHeight - margin;
                x = width - textWidth - margin;
                return new Point(x, y);

            default:
                return new Point(0, 0);

        }

    }

    protected void printText(Graphics2D graphics, int width, int height, String attributionText) {
        graphics.setFont(font);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        Rectangle2D textBounds = fontMetrics.getStringBounds(attributionText, graphics);
        int attributionWidth = (int) textBounds.getWidth();
        int attributionHeight = (int) textBounds.getHeight();

        // apply position
        Point startPoint = setStartPoint(width, height, attributionWidth, attributionHeight);
        // draw container frame
        if (framed) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(startPoint.x, startPoint.y, attributionWidth, attributionHeight);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(startPoint.x, startPoint.y, attributionWidth, attributionHeight);
        }
        // paint string
        graphics.setColor(fontColor);
        graphics.drawString(attributionText, startPoint.x, startPoint.y + fontMetrics.getAscent());
    }
}