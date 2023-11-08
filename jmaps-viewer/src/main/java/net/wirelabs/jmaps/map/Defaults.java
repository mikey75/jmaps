package net.wirelabs.jmaps.map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.Color;
import java.nio.file.Paths;

/**
 * Created 9/25/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Defaults {

    // default cache dir base
    private static final String HOME = System.getProperty("user.home");
    private static final String DEFAULT_CACHE_DIR = Paths.get(HOME, ".jmaps-cache").toString();

    // default cache dir for storing WMTS service descriptors (capabilities.xml etc)
    public static final String DEFAULT_WMTS_DESCRIPTOR_CACHE = Paths.get(DEFAULT_CACHE_DIR, "wmts-cache").toString();

    // default cache dir $HOME/.jmaps-cache/tile-cache
    public static final String DEFAULT_TILECACHE_DIR = Paths.get(DEFAULT_CACHE_DIR, "tile-cache").toString();

    // default user-agent
    public static final String DEFAULT_USER_AGENT = "JMaps Tiler v.1.0";

    // defaults for tile downloader and tile image cache
    public static final int DEFAULT_TILER_THREADS = 16;
    public static final int DEFAULT_IMGCACHE_SIZE = 8000;

    // default fill color when preparing render canvas tile
    public static final Color EMPTY_FILL_COLOR = new Color(0, 0, 0, 0);
}
