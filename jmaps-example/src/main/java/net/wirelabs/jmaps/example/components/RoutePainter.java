package net.wirelabs.jmaps.example.components;


import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.painters.Painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class RoutePainter implements Painter<MapViewer> {

    private Color routeColor = Color.RED;
    private List<Coordinate> routePoints = new ArrayList<>();

    public void setColor(Color color) {
        routeColor = color;
    }

    @Override
    public void doPaint(Graphics2D graphics, MapViewer object, int width, int height) {
        if (!routePoints.isEmpty()) {
            // store changed settings
            Stroke s = graphics.getStroke();
            Color color = graphics.getColor();

            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // do the drawing
            graphics.setColor(routeColor);
            graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawRoute(graphics, object);

            // restore changed settings
            graphics.setColor(color);
            graphics.setStroke(s);

        }
    }

    public void clearRoute() {
        routePoints.clear();
    }
    
    private void drawRoute(Graphics2D graphicsContext, MapViewer map) {

        int lastX = 0;
        int lastY = 0;

        boolean firstPoint = true;

        for (Coordinate gp : routePoints) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getBaseLayer().latLonToPixel(gp, map.getZoom());
            //!!!! trzeba odjac topleftcorner zeby uzyskac pixel na aktualnym g canvas !!!
            pt.setLocation(pt.getX() - map.getTopLeftCornerPoint().x, pt.getY() - map.getTopLeftCornerPoint().y);

            if (firstPoint) {
                firstPoint = false;
            } else {
                graphicsContext.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
            }

            lastX = (int) pt.getX();
            lastY = (int) pt.getY();
        }

    }

    public void setRoute(List<Coordinate> route) {
        this.routePoints = route;
    }

}
