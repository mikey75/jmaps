package net.wirelabs.jmaps.map.layer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LayerDefaults {

    public static final boolean SWAP_AXIS = false;
    public static final int TILE_SIZE = 256;
    public static final int MAX_ZOOM = 18;
    public static final int MIN_ZOOM = 0;
    public static final float OPACITY = 1.0f;
    public static final int ZOOM_OFFSET = 0;
}
