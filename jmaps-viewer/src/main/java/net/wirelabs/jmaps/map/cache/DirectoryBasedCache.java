package net.wirelabs.jmaps.map.cache;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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

        try {
            File f = getLocalFile(key);
            return ImageIO.read(Files.newInputStream(f.toPath()));
        } catch (IOException e) {
            return null;
        }
    }


    @Override
    public void put(String key, BufferedImage b) {
        try {
            File file = getLocalFile(key);
            if (!file.exists()) {
                Files.createDirectories(file.toPath());
            }
            writeImageToFile(b, file);
        } catch (IOException ex) {
            log.error("File cache put failed for {}", key, ex);
        }
    }

    void writeImageToFile(BufferedImage image, File file) throws IOException {
        ImageIO.write(image, "png", file);
    }

    public boolean keyExpired(String key) {

        long now = System.currentTimeMillis();
        long expirationTime = now - getCacheTimeout().toMillis();
        long lastWrittenOn;

        // if expiration time = current time -> no expiration set (cache timeout = 0) - so key never expires
        if (now == expirationTime) return false;

        try {
            File file = getLocalFile(key);
            lastWrittenOn = Files.getLastModifiedTime(file.toPath()).toMillis();
        } catch (IOException e) {
            return false;
        }
        return (lastWrittenOn < expirationTime);

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

        return new File(getBaseDir().toFile(), name);
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
