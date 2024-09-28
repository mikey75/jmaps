package net.wirelabs.jmaps.map.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;

import java.nio.file.Path;
import java.time.Duration;

@Getter
@Slf4j
public abstract class BaseCache {

    private final Path baseDir;
    private Duration cacheTimeout;

    protected BaseCache() {
        this(Defaults.DEFAULT_TILECACHE_DIR, Defaults.DEFAULT_CACHE_TIMEOUT);
    }

    protected BaseCache(Path baseDir, Duration cacheTimeout) {
        this.baseDir = baseDir;
        this.cacheTimeout = cacheTimeout;
        log.info("Secondary Tile Cache: {}", getClass().getSimpleName());

        if (!cacheTimeout.isZero()) {
            log.info("Cache expiration checking enabled! Tiles will be re-downloaded every: {}", cacheTimeout);
        }
    }

    public void setCacheTimeout(Duration duration) {
        if (duration.isZero()) {
            log.info("Disabling cache expiration checking");
        } else {
            log.info("Setting new cache timeout duration to: {}", duration);
            cacheTimeout = duration;
        }
    }
}
