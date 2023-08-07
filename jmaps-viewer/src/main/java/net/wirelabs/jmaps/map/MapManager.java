package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.MapViewer;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.map.layer.WMTSLayer;
import net.wirelabs.jmaps.map.layer.XYZLayer;
import net.wirelabs.jmaps.map.tiler.TileProvider;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created 6/3/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Manages all aspects of map loading and configuring layers
 */

@Slf4j
public class MapManager {

    private final MapViewer mapViewer;
    private final List<Layer> layers = new CopyOnWriteArrayList<>();
    
    @Getter
    private MapDefinition mapDefinition;
    
    public MapManager(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    public Layer getBaseLayer() {
        return layers.get(0);
    }
    public boolean hasLayers() {
        return !layers.isEmpty();
    }
    public List<Layer> getLayers() {
        return layers;
    }
    private void removeAllLayers(){
        for (Layer l: layers) {
            removeLayer(l);
        }
    }
    private void removeLayer(Layer layer) {
        log.info("Removing layer {}", layer.getName());
        layers.remove(layer);
        layer.getTileProvider().shutdown();
        log.info("Tile provider for layer {} shut down", layer.getName());
    }

    private void removeLayer(int idx) {
        removeLayer(layers.get(idx));
    }

    private void createLayer(LayerDefinition layerDefinition) {

        Layer layer;
        LayerType type = layerDefinition.getType();

        if (type == null) {
            throw new IllegalStateException("Layer type not given");
        }

        switch (layerDefinition.getType()) {
            case WMTS: {
                layer = new WMTSLayer(layerDefinition.getName(),layerDefinition.getUrl());
                layer.setMaxZoom(layerDefinition.getMaxZoom());
                layer.setMinZoom(layerDefinition.getMinZoom());
                layer.setSwapAxis(layerDefinition.isSwapAxis());
                layer.setOpacity(layerDefinition.getOpacity());
                layer.setZoomOffset(layerDefinition.getZoomOffset());
                break;
            }
            case XYZ: {
                layer = new XYZLayer(layerDefinition.getName(), layerDefinition.getUrl());
                layer.setMaxZoom(layerDefinition.getMaxZoom());
                layer.setMinZoom(layerDefinition.getMinZoom());
                layer.setSwapAxis(layerDefinition.isSwapAxis());
                layer.setOpacity(layerDefinition.getOpacity());
                layer.setZoomOffset(layerDefinition.getZoomOffset());
                break;
            }
            default: {
                throw new IllegalStateException("Layer type not supported");
            }
        }
        addLayer(layer);

    }

    /**
     * Creates map (layers) from map definition and sets the map definition object
     * Also sets the home position, initial zoom and map position (corner)
     * @param mapDefinition map definition
     */
    public void createMap(MapDefinition mapDefinition) {

        log.info("Setting map to {}", mapDefinition.getName());
        // there can be only one map rendered at a time
        // so remove existing if any
        removeAllLayers();
        this.mapDefinition= mapDefinition;

        for (LayerDefinition layer : mapDefinition.getLayers()) {
            createLayer(layer);
        }

        mapViewer.getTopLeftCornerPoint().setLocation(0, 0);
        mapViewer.setZoom(mapViewer.getZoom());
        mapViewer.setHomePositionSet(false);
        mapViewer.setHomePosition(mapViewer.getHome());
    }

    private void addLayer(Layer layer) {
        if (layerMatches(layer)) {
            layers.add(layer);
            layer.setTileProvider(new TileProvider(mapViewer, layer, mapViewer.getThreadCount()));
            log.info("Added layer {}, CRS:{}, TileSize:{}", layer.getName(), layer.getProjectionEngine().getCrs(), layer.getTileSize());
        }

    }

    private boolean layerMatches(Layer layerAdded) {
        // layer should have unique name, matching tilesize and crs
        for (Layer existingLayer : layers) {
            if (layerAdded.getName().equals(existingLayer.getName())) {
                log.warn("Layer mismatch: layer named {} already exists",layerAdded.getName());
                return false;
            }
            if (layerAdded.getTileSize() != existingLayer.getTileSize()) {
                log.warn("Layer mismatch: {} [tilesize]", layerAdded.getName());
                return false;
            }
            if (!layerAdded.getProjectionEngine().getCrs().equals(existingLayer.getProjectionEngine().getCrs())) {
                log.warn("Layer mismatch: {} [crs]", layerAdded.getName());
                return false;
            }
        }
        return true;
    }
}
