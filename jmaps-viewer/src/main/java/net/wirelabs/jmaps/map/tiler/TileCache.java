package net.wirelabs.jmaps.map.tiler;

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
public class TileCache implements Cache<String,BufferedImage> {
    
    private final ConcurrentLinkedHashMap<String,BufferedImage> map;

    public TileCache(int capacity) {

        this.map = new ConcurrentLinkedHashMap.Builder<String,BufferedImage>()
                .maximumWeightedCapacity(capacity)
                .build();

        log.info("TileCache: capacity {} entries, LRU", capacity);
    }

    public BufferedImage get(String key) {
        return map.get(key);
    }

    public void put(String key, BufferedImage value) {
        map.put(key, value);
    }

    public long size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }
}
