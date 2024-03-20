package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.layer.*;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import net.wirelabs.jmaps.map.readers.MapReader;

import java.io.File;

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
        MapDefinition mapDefinition = MapReader.loadMapDefinitionFile(xmlMapFile);
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

        Layer layer;
        LayerType type = layerDefinition.getType();

        if (type == null ) {
            throw new CriticalMapException("Layer type not given or unknown");
        }

        switch (type) {
            case WMTS: {
               layer = new WMTSLayer(layerDefinition);
               return layer;
            }
            case XYZ: {
                layer = new XYZLayer(layerDefinition);
                return layer;
            }
            case VE: {
                layer = new VEarthLayer(layerDefinition);
                return layer;
            }
            default: {
                throw new CriticalMapException("Layer type not currently supported");
            }
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
