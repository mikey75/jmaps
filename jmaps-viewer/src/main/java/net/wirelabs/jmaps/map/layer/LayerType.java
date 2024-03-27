package net.wirelabs.jmaps.map.layer;

import lombok.RequiredArgsConstructor;

/**
 * Created 6/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@RequiredArgsConstructor
public enum LayerType {

    XYZ(XYZLayer.class),        // osm style
    WMTS(WMTSLayer.class),      // wmts
    VE(VEarthLayer.class)      // ms virtual earth
    ;

    public final Class<? extends Layer> layer;

}
