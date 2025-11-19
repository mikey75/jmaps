package net.wirelabs.jmaps.map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Created 9/25/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Defaults {

    public static final Duration DEFAULT_CACHE_TIMEOUT = Duration.ofDays(30);

    // defaults for redis cache
    public static final int DEFAULT_REDIS_POOLSIZE = 100;
    public static final String DEFAULT_REDIS_HOST = "localhost";
    public static final int DEFAULT_REDIS_PORT = 6379;

    // default cache dir base
    private static final String HOME = System.getProperty("user.home");
    private static final Path DEFAULT_CACHE_DIR = Paths.get(HOME, ".jmaps-cache");

    // default cache dir for storing WMTS service descriptors (capabilities.xml etc)
    public static final Path DEFAULT_WMTS_DESCRIPTOR_CACHE = Paths.get(DEFAULT_CACHE_DIR.toString(), "wmts-cache");

    // default cache dir for directory based cache -> $HOME/.jmaps-cache/tile-cache
    public static final Path DEFAULT_TILE_CACHE_DIR = Paths.get(DEFAULT_CACHE_DIR.toString(), "tile-cache");
    // default cache dir for derby DB based cache -> $HOME/.jmaps-cache/tile-cache-db
    public static final Path DEFAULT_TILE_CACHE_DB = Paths.get(DEFAULT_CACHE_DIR.toString(), "tile-cache-db");

    // default user-agent
    public static final String DEFAULT_USER_AGENT = "JMaps Tiler v.1.0";

    // defaults for tile downloader and tile image cache
    public static final int DEFAULT_TILER_THREADS = 16;
    public static final int DEFAULT_IMG_CACHE_SIZE = 32000;

    // default fill color when preparing render canvas tile
    public static final Color EMPTY_FILL_COLOR = new Color(0, 0, 0, 0);
}
