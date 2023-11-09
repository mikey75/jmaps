package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.layer.*;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created 11/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@Getter
public class MapManager {

    // model for map object
    private String mapName = "[no name defined]";
    private String mapCopyrightAttribution = "[no attribution in definition]";
    private final List<Layer> layers = new CopyOnWriteArrayList<>();


    public Layer getBaseLayer() {
        return layers.get(0);
    }

    public boolean layersPresent() {
        return !layers.isEmpty();
    }

    public List<Layer> getEnabledLayers() {
        return layers.stream()
                .filter(Layer::isEnabled)
                .collect(Collectors.toList());
    }

    public void createMap(MapDefinition mapDefinition) {
        log.info("Setting map to {}", mapDefinition.getName());

        mapName = mapDefinition.getName();
        mapCopyrightAttribution = mapDefinition.getCopyright();
        createLayers(mapDefinition);

    }

    public int getMaxZoom() {
        return layers.stream()
                .map(Layer::getMaxZoom)
                .mapToInt(v -> v)
                .min().orElse(0);

    }

    public int getMinZoom() {
        return layers
                .stream()
                .map(Layer::getMinZoom)
                .mapToInt(val -> val)
                .max().orElse(0);


    }

    private void createLayers(MapDefinition mapDefinition) {
        List<LayerDefinition> layerDefinitions = mapDefinition.getLayers();
        layers.clear();
        for (LayerDefinition layerDefinition : layerDefinitions) {
            Layer mapLayer = createMapLayerFromDefinition(layerDefinition);
            if (layerMatches(mapLayer)) {
                layers.add(mapLayer);
                log.info("Added layer {}, CRS:{}, TileSize:{}", mapLayer.getName(), mapLayer.getProjectionEngine().getCrs(), mapLayer.getTileSize());
            } else {
                log.error("Layer not added!");
            }
        }

    }

    boolean isMultilayer() {
        return layers.size() > 1;
    }

    private Layer createMapLayerFromDefinition(LayerDefinition layerDefinition) {

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
        return layer;


    }

    private boolean layerMatches(Layer layerAdded) {
        // layer should have unique name, matching tilesize and crs
        for (Layer existingLayer : layers) {
            if (layerAdded.getName().equals(existingLayer.getName())) {
                log.warn("Layer mismatch: layer named {} already exists", layerAdded.getName());
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
