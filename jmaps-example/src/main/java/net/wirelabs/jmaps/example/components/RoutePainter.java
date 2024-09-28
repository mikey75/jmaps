package net.wirelabs.jmaps.example.components;


import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.painters.Painter;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class RoutePainter extends Painter<MapViewer> {

    private Color routeColor = Color.RED;

    public void setColor(Color color) {
        routeColor = color;
    }

    @Override
    public void doPaint(Graphics2D graphics, MapViewer mapViewer, int width, int height) {
        if (!getObjects().isEmpty()) {
            // store changed settings
            Stroke s = graphics.getStroke();
            Color color = graphics.getColor();
            // not sure if needed
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            // do the drawing
            graphics.setColor(routeColor);
            graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            drawRoute(graphics, mapViewer);
            // restore changed settings
            graphics.setColor(color);
            graphics.setStroke(s);

        }
    }



    public void clearRoute() {
        getObjects().clear();
    }
    
    private void drawRoute(Graphics2D graphicsContext, MapViewer map) {

        int lastX = 0;
        int lastY = 0;

        boolean firstPoint = true;

        for (Coordinate gp : getObjects()) {
            // convert geo-coordinate to world bitmap pixel
            Point2D pt = map.getCurrentMap().getBaseLayer().latLonToPixel(gp, map.getZoom());
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
        setObjects(route);
    }

}
