package net.wirelabs.jmaps.map.cache;

import java.awt.image.BufferedImage;

/**
 * Dummy cache - no op cache implementation,
 * plugged in by default so no 2level cache is present.
 */
public class DummyCache implements Cache<String, BufferedImage> {

    @Override
    public BufferedImage get(String key) {
        return null;
    }

    @Override
    public void put(String key, BufferedImage value) {
        // dummy no op
    }

    @Override
    public void clear() {
        // dummy no op
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

}
