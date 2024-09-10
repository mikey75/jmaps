package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.model.map.LayerDocument;

public class QuadLayer extends XYZLayer {

    public QuadLayer(LayerDocument.Layer layerDefinition) {
        super(layerDefinition);
    }
    /**
     * {@inheritDoc}
     */


    @Override
    public String createTileUrl(int x, int y, int zoom) {

        final String quadKey = tileToQuadKey(x, y,  zoom);

        return url.replace("{quad}", quadKey)
                .replace("{quadchar}", String.valueOf(quadKey.charAt(quadKey.length() - 1)));

    }

    private String tileToQuadKey(final int x, final int y, final int zoom) {

        StringBuilder quadKey = new StringBuilder();

        for (int z = zoom; z > 0; z--) {
            int mask = 1 << (z - 1);
            int digit = 0;

            if ((x & mask) != 0) {
                digit++;
            }

            if ((y & mask) != 0) {
                digit += 2;
            }

            quadKey.append(digit);
        }

        return quadKey.toString();
    }


}
