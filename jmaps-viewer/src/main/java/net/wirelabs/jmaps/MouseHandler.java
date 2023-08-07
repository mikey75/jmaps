package net.wirelabs.jmaps;

import lombok.Getter;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

/**
 * Created 6/7/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MouseHandler extends MouseInputAdapter implements MouseWheelListener {

    private Point prevMousePosition;
    private Cursor priorCursor;
    private final MapViewer mapViewer;
    @Getter
    private final Point mousePoint = new Point();

    public MouseHandler(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
        mapViewer.addMouseMotionListener(this);
        mapViewer.addMouseListener(this);
        mapViewer.addMouseWheelListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {


        if (mapViewer.getMapManager().hasLayers()) {
            Layer baseLayer = mapViewer.getMapManager().getBaseLayer();
            // location of mouse at current zoom
            Coordinate mouseLatLon = baseLayer.pixelToLatLon(mousePoint, mapViewer.getZoom());

            int zoom = mapViewer.getZoom() - evt.getWheelRotation();

            if (zoom >= baseLayer.getMinZoom() && zoom <= baseLayer.getMaxZoom()) {
                // get mouse location in pixels in new zoom
                Point2D p = baseLayer.latLonToPixel(mouseLatLon, zoom);

                // update top left corner to new zoom
                updateTopLeftCornerPoint(evt, p);
                // update mouse point
                updateMousePoint(evt);

                // set new zoom
                mapViewer.setZoom(zoom);
                mapViewer.repaint();
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        updateMousePoint(mouseEvent);
        mapViewer.repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (!SwingUtilities.isLeftMouseButton(mouseEvent))
            return;

        prevMousePosition = mouseEvent.getPoint();
        priorCursor = mapViewer.getCursor();
        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (!SwingUtilities.isLeftMouseButton(mouseEvent))
            return;

        Point currentMousePosition = mouseEvent.getPoint();
        if (prevMousePosition != null) {


            Point topLeftCornerPoint = mapViewer.getTopLeftCornerPoint();

            int deltaX = prevMousePosition.x - currentMousePosition.x;
            int deltaY = prevMousePosition.y - currentMousePosition.y;

            topLeftCornerPoint.translate(deltaX, deltaY);

            clipToBounds(topLeftCornerPoint);

            prevMousePosition = currentMousePosition;

            updateMousePoint(mouseEvent);
            mapViewer.repaint();

        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (!SwingUtilities.isLeftMouseButton(evt))
            return;
        prevMousePosition = null;
        mapViewer.setCursor(priorCursor);
        mapViewer.repaint();

    }

    private void clipToBounds(Point topLeftCornerPoint) {
        if (mapViewer.getMapManager().hasLayers()) {
            Layer baseLayer = mapViewer.getMapManager().getBaseLayer();

            int zoom = mapViewer.getZoom();
            int maxX = baseLayer.getMapSizeInPixels(zoom).width - mapViewer.getWidth();
            int maxY = baseLayer.getMapSizeInPixels(zoom).height - mapViewer.getHeight();

            if (topLeftCornerPoint.x < 0) {
                topLeftCornerPoint.x = 0;
            }
            if (topLeftCornerPoint.y < 0) {
                topLeftCornerPoint.y = 0;
            }

            if (topLeftCornerPoint.x >= maxX) {
                topLeftCornerPoint.x = maxX;
            }
            if (topLeftCornerPoint.y >= maxY) {
                topLeftCornerPoint.y = maxY;
            }
        }
    }

    private void updateMousePoint(MouseEvent mouseEvent) {
        mousePoint.x = mouseEvent.getX() + mapViewer.getTopLeftCornerPoint().x;
        mousePoint.y = mouseEvent.getY() + mapViewer.getTopLeftCornerPoint().y;
    }

    private void updateTopLeftCornerPoint(MouseWheelEvent mouseEvent, Point2D point) {
        mapViewer.getTopLeftCornerPoint().x = (int) point.getX() - mouseEvent.getX();
        mapViewer.getTopLeftCornerPoint().y = (int) point.getY() - mouseEvent.getY();
    }
}

