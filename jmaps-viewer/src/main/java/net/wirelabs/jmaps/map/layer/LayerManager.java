package net.wirelabs.jmaps.map.layer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created 6/3/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Manages all aspects of layers
 */

@Slf4j
@Getter
public class LayerManager {

    private final List<Layer> layers = new CopyOnWriteArrayList<>();

    public Layer getBaseLayer() {
        return layers.get(0);
    }

    public boolean layersPresent() {
        return !layers.isEmpty();
    }

    public void removeAllLayers(){
        for (Layer l: layers) {
            removeLayer(l);
        }
    }

    public void createLayer(LayerDefinition layerDefinition) {

        Layer layer;
        LayerType type = layerDefinition.getType();

        if (type == null) {
            throw new IllegalStateException("Layer type not given");
        }

        switch (layerDefinition.getType()) {
            case WMTS: {
                layer = new WMTSLayer(layerDefinition);
                break;
            }
            case XYZ: {
                layer = new XYZLayer(layerDefinition);
                break;
            }
            default: {
                throw new IllegalStateException("Layer type not supported");
            }
        }
        addLayer(layer);

    }

    private void removeLayer(Layer layer) {
        log.info("Removing layer {}", layer.getName());
        layers.remove(layer);
    }

    private void addLayer(Layer layer) {
        if (layerMatches(layer)) {
            layers.add(layer);
            log.info("Added layer {}, CRS:{}, TileSize:{}", layer.getName(), layer.getProjectionEngine().getCrs(), layer.getTileSize());
        } else {
            log.error("Layer not added!");
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

    public boolean isMultilayer() {
        return layers.size() > 1;
    }

    public List<Layer> getEnabledLayers() {
        return layers.stream()
                .filter(Layer::isEnabled)
                .collect(Collectors.toList());
    }
}
