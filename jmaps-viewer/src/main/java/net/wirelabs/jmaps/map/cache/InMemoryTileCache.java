package net.wirelabs.jmaps.map.cache;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.extern.slf4j.Slf4j;


import java.awt.image.BufferedImage;

/**
 * Created 1/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * LRU Tile cache based on ConcurrentLinkedHashMapLru class
 * by google.
 */
@Slf4j
public class InMemoryTileCache implements Cache<String,BufferedImage> {
    
    private final ConcurrentLinkedHashMap<String,BufferedImage> lruCache;

    public InMemoryTileCache(int initialCapacity) {

        this.lruCache = new ConcurrentLinkedHashMap.Builder<String,BufferedImage>()
                .maximumWeightedCapacity(initialCapacity)
                .build();
    }

    @Override
    public BufferedImage get(String key) {
        return lruCache.get(key);
    }

    @Override
    public void put(String key, BufferedImage value) {
        lruCache.put(key, value);
    }

    @Override
    public void clear() {
        lruCache.clear();
    }

    @Override
    public boolean contains(String key) {
        return lruCache.containsKey(key);
    }

}
