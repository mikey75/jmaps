package net.wirelabs.jmaps.example.components;

import lombok.Getter;
import net.wirelabs.jmaps.map.geo.Coordinate;

@Getter
public enum ExampleMap {

    OSM("Open Street Map", "OpenStreetMap.xml"),
    CYCLE("Cyclo OSM", "CyclOSM.xml"),
    MTB("MTB Map", "MTBMap.xml"),
    TOPO_CZ("Czech Topographic, WMTS", "CzechTopoWMTS.xml"),
    TOPO_PL("Poland Topographic, WMTS", "GeoportalBDOT.xml"),
    TOPO_PL_RASTER("Poland Topo Raster, WMTS", "GeoportalTopoRaster.xml"),
    TOPO_HIPSO("Poland Topo + Hipso, WMTS", "GeoportalLayered.xml"),
    VIRT_EARTH_SAT("Virtual Earth Satelite", "VESat.xml"),
    VIRT_EARTH_MAP("Virtual Earth Map", "VEMap.xml"),
    VIRT_EARTH_HYB("Virtual Earth Hybrid", "VEHyb.xml");

    private final String name;
    private final String mapFile;

    ExampleMap(String name, String mapFile) {
        this.name = name;
        this.mapFile = mapFile;
    }
}
