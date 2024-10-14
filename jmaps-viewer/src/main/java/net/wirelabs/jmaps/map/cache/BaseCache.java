package net.wirelabs.jmaps.map.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;

import java.nio.file.Path;
import java.time.Duration;

@Getter
@Setter
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
        log.info("Secondary Tile Cache: {}, location: {}", getClass().getSimpleName(), getBaseDir());

        if (isCacheTimeoutEnabled()) {
            String expirationTimeMsg = prepareExpirationMessage(cacheTimeout);
            log.info("Cache expiration checking enabled! Tiles will be re-downloaded every: {}", expirationTimeMsg);
        } else {
            log.info("Cache expiration checking disabled. Stored tiles won't ever be re-downloaded");
        }
    }

    private String prepareExpirationMessage(Duration cacheTimeout) {

        StringBuilder builder = new StringBuilder();

        long days = cacheTimeout.toDaysPart();
        long hours = cacheTimeout.toHoursPart();
        long minutes = cacheTimeout.toMinutesPart();
        long seconds = cacheTimeout.toSecondsPart();

        if (days > 0) builder.append(days).append(" days");
        if (hours > 0) builder.append(hours).append(" hours");
        if (minutes > 0) builder.append(minutes).append(" minutes");
        if (seconds > 0) builder.append(seconds).append(" seconds");

        return builder.toString();
    }

    protected boolean isCacheTimeoutEnabled() {
        return !cacheTimeout.isZero();
    }

    protected boolean keyExpired(long timestamp) {
        // key expires when cache timeout is enabled, and the
        // now() is greater than file/entity timestamp plus cache timeout
        boolean timeExpired = timestamp != 0 && System.currentTimeMillis() > (timestamp + getCacheTimeout().toMillis());
            return isCacheTimeoutEnabled() && timeExpired;

    }
}
