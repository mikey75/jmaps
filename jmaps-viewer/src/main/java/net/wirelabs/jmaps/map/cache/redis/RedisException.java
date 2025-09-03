package net.wirelabs.jmaps.map.cache.redis;

import java.io.IOException;

public final class RedisException extends IOException {
    public RedisException(String message) {
        super(message);
    }
}