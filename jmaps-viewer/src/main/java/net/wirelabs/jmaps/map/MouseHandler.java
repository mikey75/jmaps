package net.wirelabs.jmaps.map;

import lombok.Getter;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.layer.Layer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Created 6/7/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MouseHandler extends MouseInputAdapter implements MouseWheelListener {


    private final MapViewer mapViewer;

    private Cursor priorCursor;
    private final Point topLeftCorner;

    @Getter
    private final Point currentMousePosition = new Point();
    private final Point prevMousePosition = new Point();

    public MouseHandler(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
        this.topLeftCorner = mapViewer.getTopLeftCornerPoint();

        mapViewer.addMouseMotionListener(this);
        mapViewer.addMouseListener(this);
        mapViewer.addMouseWheelListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent evt) {

        if (mapViewer.getCurrentMap().layersPresent()) {

            updateMousePoint(evt);

            Layer baseLayer = mapViewer.getCurrentMap().getBaseLayer();
            // location of mouse at current zoom
            Coordinate mouseLatLon = baseLayer.pixelToLatLon(currentMousePosition, mapViewer.getZoom());

            int zoom = mapViewer.getZoom() - evt.getWheelRotation();

            int minZoomAllLayers = mapViewer.getCurrentMap().getMinZoom();
            int maxZoomAllLayers = mapViewer.getCurrentMap().getMaxZoom();

            if (zoom >= minZoomAllLayers && zoom <= maxZoomAllLayers) {
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

        prevMousePosition.setLocation(mouseEvent.getPoint());
        priorCursor = mapViewer.getCursor();
        mapViewer.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (!SwingUtilities.isLeftMouseButton(mouseEvent))
            return;

        Point currentMousePoint = mouseEvent.getPoint();

            int deltaX = prevMousePosition.x - currentMousePoint.x;
            int deltaY = prevMousePosition.y - currentMousePoint.y;

            topLeftCorner.translate(deltaX, deltaY);

            clipToBounds();

            prevMousePosition.setLocation(currentMousePoint);

            updateMousePoint(mouseEvent);
            mapViewer.repaint();

    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        if (!SwingUtilities.isLeftMouseButton(evt))
            return;
        prevMousePosition.setLocation(0,0);
        mapViewer.setCursor(priorCursor);

    }

    private void clipToBounds() {
        if (mapViewer.getCurrentMap().layersPresent()) {

            int zoom = mapViewer.getZoom();
            int maxX = mapViewer.getMapSizeInPixels(zoom).width - mapViewer.getWidth();
            int maxY = mapViewer.getMapSizeInPixels(zoom).height - mapViewer.getHeight();

            if (topLeftCorner.x < 0) {
                topLeftCorner.x = 0;
            }
            if (topLeftCorner.y < 0) {
                topLeftCorner.y = 0;
            }

            if (topLeftCorner.x >= maxX) {
                topLeftCorner.x = maxX;
            }
            if (topLeftCorner.y >= maxY) {
                topLeftCorner.y = maxY;
            }
        }
    }

    private void updateMousePoint(MouseEvent mouseEvent) {
        currentMousePosition.setLocation(
                mouseEvent.getX() + topLeftCorner.x,
                mouseEvent.getY() + topLeftCorner.y
        );
    }

    private void updateTopLeftCornerPoint(MouseWheelEvent mouseEvent, Point2D point) {
        topLeftCorner.setLocation(
                (int) point.getX() - mouseEvent.getX(),
                (int) point.getY() - mouseEvent.getY()
        );
    }
}

