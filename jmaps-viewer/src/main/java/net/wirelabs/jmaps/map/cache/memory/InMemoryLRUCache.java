package net.wirelabs.jmaps.map.cache.memory;

import com.github.benmanes.caffeine.cache.*;
import net.wirelabs.jmaps.map.Defaults;


import java.awt.image.*;

public class InMemoryLRUCache {

    private final Cache<String, BufferedImage> inMemoryCache = Caffeine.newBuilder()
            .maximumSize(Defaults.DEFAULT_IMG_CACHE_SIZE)
            .build();

    public BufferedImage get(String key) {
        return inMemoryCache.getIfPresent(key);
    }

    public void put(String key, BufferedImage value) {
        inMemoryCache.put(key, value);
    }

    public void clear() {
        inMemoryCache.invalidateAll();
        inMemoryCache.cleanUp();
    }
}
