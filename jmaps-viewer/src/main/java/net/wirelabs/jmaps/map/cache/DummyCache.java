package net.wirelabs.jmaps.map.cache;

import java.awt.image.BufferedImage;

/**
 * Dummy cache - no op cache implementation
 */
public class DummyCache implements Cache<String, BufferedImage> {

    @Override
    public BufferedImage get(String key) {
        return null;
    }

    @Override
    public void put(String key, BufferedImage value) {

    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(String key) {
        return false;
    }

}
