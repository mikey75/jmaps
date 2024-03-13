package net.wirelabs.jmaps.map.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Created 1/2/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class DirectoryBasedCache implements Cache<String, BufferedImage> {

    private final Path baseDir;
    @Setter @Getter
    private Duration validityTime = Defaults.DEFAULT_CACHE_VALIDITY_TIME;

    public DirectoryBasedCache() {
        this(Defaults.DEFAULT_TILECACHE_DIR);
    }

    public DirectoryBasedCache(String cacheDir) {
        this.baseDir = Paths.get(cacheDir);
    }

    @Override
    public BufferedImage get(String key) {

        try {
            File f = getLocalFile(key);
            return ImageIO.read(Files.newInputStream(f.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    private boolean fileExpired(File f) {

        long expirationTime = System.currentTimeMillis() - validityTime.toMillis();
        long lastWrittenOn;

        try {
            lastWrittenOn = Files.getLastModifiedTime(f.toPath()).toMillis();
        } catch (IOException e) {
            return false;
        }
        return  (lastWrittenOn < expirationTime);

    }

    @Override
    public void put(String key, BufferedImage b) {
        try {
            File file = getLocalFile(key);
            if (!file.exists()) {
                Files.createDirectories(file.toPath());
            }
            ImageIO.write(b, "png",file);
        } catch (IOException ex) {
            log.error("File cache put failed for {}", key, ex);
        }
    }

    @Override
    public boolean keyExpired(String key) {
        File file = getLocalFile(key);
        return fileExpired(file);
    }

    private File getLocalFile(String remoteUri) {
        URI uri = URI.create(remoteUri);
        StringBuilder sb = new StringBuilder();

        String host = uri.getHost();
        String query = uri.getQuery();
        String path = uri.getPath();

        if (host != null) {
            sb.append(host);
        }
        if (path != null) {
            sb.append(path);
        }
        if (query != null) {
            sb.append('?');
            sb.append(query);
        }

        String name;

        final int maxLen = 250;

        if (sb.length() < maxLen) {
            name = sb.toString();
        } else {
            name = sb.substring(0, maxLen);
        }
        name = normalizeUrl(name);

        return new File(baseDir.toFile(), name);
    }

    private String normalizeUrl(String name) {

        char replacementChar = '$';
        char[] charsNormalized = new char[]{'&', '?', '*', ':', '<', '>', '"'};

        for (char nchar : charsNormalized) {
            name = name.replace(nchar, replacementChar);
        }
        return name;
    }
}
