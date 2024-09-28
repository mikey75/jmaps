package net.wirelabs.jmaps.map.downloader;

import java.awt.image.*;

public interface TileProvider {
    BufferedImage getTile(String url);
}
