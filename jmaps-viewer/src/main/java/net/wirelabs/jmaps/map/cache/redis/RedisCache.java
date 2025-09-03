package net.wirelabs.jmaps.map.cache.redis;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.BaseCache;
import net.wirelabs.jmaps.map.cache.Cache;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class RedisCache extends BaseCache implements Cache<String, BufferedImage> {

    private final RedisClient client;

    public RedisCache(String host, int port, Duration expirationTime, int connectionPoolSize) {
        super(Path.of("localhost@" + port), expirationTime);
        this.client = new RedisClient(host, port, expirationTime, connectionPoolSize);
    }

    @Override
    public BufferedImage get(String key) {
        try {
            return client.getImage(key);
        } catch (IOException e) {
            log.warn("Redis cache get failed for {}-{}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, BufferedImage value) {

        try {
            client.putImage(key, value, "png");
        } catch (IOException e) {
            log.error("Redis cache put failed for {}-{}", key, e.getMessage());
        }
    }

    @Override
    public boolean keyExpired(String key) {
        return false;
    }
}
