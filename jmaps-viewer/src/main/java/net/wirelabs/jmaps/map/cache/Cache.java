package net.wirelabs.jmaps.map.cache;

import java.time.Duration;

/**
 * Created 1/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public interface Cache<K,V> {
    V get(K key);
    void put(K key, V value);
    boolean keyExpired(K key);
    void setValidityTime(Duration duration);
}
