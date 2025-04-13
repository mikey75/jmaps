package net.wirelabs.jmaps.map.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.utils.UrlUtils;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Created 1/2/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@Getter
public class DirectoryBasedCache extends BaseCache implements Cache<String, BufferedImage> {

    public DirectoryBasedCache() {
        super();
    }

    public DirectoryBasedCache(Path cacheDir, Duration cacheTimeout) {
        super(cacheDir, cacheTimeout);
    }

    @Override
    public BufferedImage get(String key) {
        return getImage(key);
    }

    @Override
    public void put(String key, BufferedImage b) {
        putImage(key, b);
    }

    private BufferedImage getImage(String key) {
        try {
            Path filePath = getLocalFile(key);
            // if file does not exists - return immediately
            if (!filePath.toFile().exists()) {
                return null;
            } else {
                return ImageIO.read(Files.newInputStream(filePath));
            }
        } catch (IOException e) {
            // exception on file read - this might be emergency - file exists but cannot be read, warn
            log.warn("File cache get failed for {}", key);
            return null;
        }
    }


    private void putImage(String key, BufferedImage b) {
        try {
            Path filePath = getLocalFile(key);
            // create file only if it does not exist so that if the entry expires,
            // file is not recreated  (saves time) and only image is written to this existing file
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath);
            }
            ImageIO.write(b, "png", filePath.toFile());

        } catch (IOException ex) {
            // exception on file put might be a filesystem issue emergency - warn
            log.warn("File cache put failed for {}", key);
        }
    }

    public boolean keyExpired(String key) {
        return keyExpired(getTimestampFromFile(key));
    }

    private long getTimestampFromFile(String key) {
        try {
            return Files.getLastModifiedTime(getLocalFile(key)).toMillis();
        } catch (IOException e) {
            return 0;
        }
    }
    /**
     * This is basically a method to convert tile url to a file cache key
     * being the path for the place to store the file
     */
    private Path getLocalFile(String remoteUri) {
        return Path.of(getBaseDir().toString(),UrlUtils.urlToStringPath(remoteUri));
    }

}
