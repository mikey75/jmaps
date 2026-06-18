package net.wirelabs.jmaps.map.cache;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
@Slf4j
@NoArgsConstructor
public class BoundsCache {
    @Setter
    private Path baseDir = Defaults.DEFAULT_BOUNDS_CACHE;

    public void put(String filename, String content) {
        try {
            Files.createDirectories(baseDir);
            Files.writeString(Path.of(baseDir.toString(),filename), content);
        } catch (IOException ex) {
            log.warn("File cache put failed for {}:{}", filename, ex.getMessage());
        }
    }

    public String get(String filename) {
        try {
            return Files.readString(Path.of(baseDir.toString(),filename));
        } catch (IOException ex) {
            log.warn("Cache get failed for file {}:{}", filename, ex.getMessage());
            return Strings.EMPTY;
        }
    }
}
