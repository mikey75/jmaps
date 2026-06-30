package net.wirelabs.jmaps.map.cache;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class BoundsCache {

    private final Path baseDir;

    public BoundsCache() {
        this(Defaults.DEFAULT_BOUNDS_CACHE);
    }
    public BoundsCache(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void put(String filename, String content) {
        try {
            Files.createDirectories(baseDir);
            Files.writeString(Path.of(baseDir.toString(),filename), content);
        } catch (IOException ex) {
            log.warn("File cache put failed for {}", filename, ex);
        }
    }

    public String get(String filename) {
        try {
            return Files.readString(Path.of(baseDir.toString(),filename));
        } catch (IOException ex) {
            log.warn("Cache get failed for file {}", filename, ex);
            return Strings.EMPTY;
        }
    }
}
