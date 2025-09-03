package net.wirelabs.jmaps.map.cache;

import lombok.extern.slf4j.Slf4j;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class RedisCache extends BaseCache implements Cache<String, BufferedImage> {

    private final String host;
    private final int port;
    private Duration expirationTime;

    public RedisCache(String host, int port, Duration expirationTime) {
        super(Path.of("localhost@" + port),expirationTime);
        this.host = host;
        this.port = port;
        this.expirationTime = expirationTime;
    }

    @Override
    public BufferedImage get(String key) {
        try (SimpleRedisClient redisClient = new SimpleRedisClient(host,port)) {
            return redisClient.getImage(key);
        } catch (IOException e) {
            log.error("Could not get image {} from cache: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void put(String key, BufferedImage value) {
        try (SimpleRedisClient redisClient = new SimpleRedisClient(host,port)) {
            redisClient.setImage(key, value, "png");
        } catch (IOException e) {
            log.error("Can't put image {} into cache: {}", key, e.getMessage());
        }
    }

    @Override
    public boolean keyExpired(String key) {
        return false;
    }
}
