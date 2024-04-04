package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

import java.io.File;

import static net.wirelabs.jmaps.map.readers.MapReader.loadMapDefinitionFile;

/**
 * Created 11/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Create mapviewer-usable map object
 * from map xml definition file
 */
@Getter
@Slf4j
public class MapCreator {

    public MapObject createMap(File xmlMapFile) {

        MapObject map = new MapObject();

        MapDefinition mapDefinition = loadMapDefinitionFile(xmlMapFile);
        log.info("Creating map: [{}]", mapDefinition.getName());

        map.setMapName(mapDefinition.getName());
        map.setMapCopyrightAttribution(mapDefinition.getCopyright());

        for (LayerDefinition layerDefinition : mapDefinition.getLayers()) {
            Layer mapLayer = createMapLayer(layerDefinition);
            if (layerMatchesExistingLayers(map, mapLayer)) {
                map.addLayer(mapLayer);
                log.info("Added layer {}, CRS:{}, TileSize:{}", mapLayer.getName(), mapLayer.getProjectionEngine().getCrs(), mapLayer.getTileSize());
            } else {
                log.error("Layer not added!");
            }
        }
        return map;

    }

    private Layer createMapLayer(LayerDefinition layerDefinition) {

        try {
            LayerType type = layerDefinition.getType();
            return type.layer.getDeclaredConstructor(LayerDefinition.class).newInstance(layerDefinition);
        } catch (Exception e) {
            log.info("Layer type not given or unknown");
            throw new CriticalMapException("Layer type not given or unknown");
        }

    }

    // check if added layer matches current map layers
    private boolean layerMatchesExistingLayers(MapObject map, Layer layerAdded) {
        // layer should have unique name, matching tilesize and crs
        for (Layer existingLayer : map.getLayers()) {
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
