package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.map.model.map.LayerDefinition;

public class VEarthLayer extends XYZLayer {

    public VEarthLayer(LayerDefinition layerDefinition) {
        super(layerDefinition);
    }

    @Override
    public String createTileUrl(int x, int y, int zoom) {

        final String quad = tileToQuadKey(x, y,  zoom);

        return url.replace("{quad}", quad)
                .replace("{quadchar}", String.valueOf(quad.charAt(quad.length() - 1)));

    }

    private String tileToQuadKey(final int tx, final int ty, final int zl) {
        StringBuilder quad = new StringBuilder();

        for (int i = zl; i > 0; i--) {
            int mask = 1 << (i - 1);
            int cell = 0;

            if ((tx & mask) != 0) {
                cell++;
            }

            if ((ty & mask) != 0) {
                cell += 2;
            }

            quad.append(cell);
        }

        return quad.toString();
    }

}
