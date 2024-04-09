package net.wirelabs.jmaps.map.downloader;

import java.awt.image.BufferedImage;

public interface TileProvider {
    BufferedImage getTile(String url);
}
