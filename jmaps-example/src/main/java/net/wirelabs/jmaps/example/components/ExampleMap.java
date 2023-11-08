package net.wirelabs.jmaps.example.components;

import lombok.Getter;
import net.wirelabs.jmaps.map.geo.Coordinate;

@Getter
public enum ExampleMap {

    OSM("Open Street Map", "OpenStreetMap.xml", new Coordinate(22.565628, 51.247717)),
    CYCLE("Cyclo OSM", "CyclOSM.xml", new Coordinate(22.565628, 51.247717)),
    MTB("MTB Map", "MTBMap.xml", new Coordinate(22.565628, 51.247717)),
    TOPO_CZ("Czech Topographic, WMTS", "CzechTopoWMTS.xml", null), //new Coordinate(14.4250, 50.0836)),
    TOPO_PL("Poland Topographic, WMTS", "GeoportalBDOT.xml", new Coordinate(22.565628, 51.247717)),
    TOPO_PL_RASTER("Poland Topo Raster, WMTS", "GeoportalTopoRaster.xml", new Coordinate(22.565628, 51.247717)),
    TOPO_HIPSO("Poland Topo + Hipso, WMTS", "GeoportalLayered.xml", new Coordinate(22.565628, 51.247717));

    private final String name;
    private final String mapFile;
    private final Coordinate centerON;

    ExampleMap(String name, String mapFile, Coordinate centerON) {
        this.name = name;
        this.mapFile = mapFile;
        this.centerON = centerON;
    }
}
