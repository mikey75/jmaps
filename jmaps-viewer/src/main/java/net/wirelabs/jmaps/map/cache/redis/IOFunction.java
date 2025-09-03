package net.wirelabs.jmaps.map.cache.redis;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T, R> {
    R apply(T t) throws IOException;
}