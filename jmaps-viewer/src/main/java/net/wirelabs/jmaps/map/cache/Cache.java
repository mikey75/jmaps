package net.wirelabs.jmaps.map.cache;

/**
 * Created 1/4/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public interface Cache<K,V> {
    V get(K key);
    void put(K key, V value);
    long size();
    void clear();
    boolean contains(K key);

}
