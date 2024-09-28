package net.wirelabs.jmaps.map;

import java.awt.event.*;

public class RecenterMapListener implements ComponentListener {

    private final MapViewer mapViewer;

    public RecenterMapListener(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        mapViewer.centerMapOrBestFit();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // no need to recenter
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // no need to recenter
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // no need to recenter
    }
}
