package net.wirelabs.jmaps.viewer.map.tiler;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created 1/2/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class DirectoryBasedCache implements Cache<String, BufferedImage> {

    @Setter
    private Path baseDir;

    public DirectoryBasedCache(String cacheDir) {
        this.baseDir = Paths.get(cacheDir);
    }


    @Override
    public BufferedImage get(String key) {
        File f = getCacheFile(key);

        try {
            BufferedImage image = ImageIO.read(Files.newInputStream(f.toPath()));
            if (image != null) {
                return image;
            }
        } catch (IOException e) {
            return null;
        }
        return null;

    }

    @Override
    public void put(String key, BufferedImage b) {
        try {
            File file = getCacheFile(key);
            if (file.exists()) return;
            Files.createDirectories(file.toPath());
            ImageIO.write(b, "png", getCacheFile(key));
        } catch (IOException ex) {
            log.error("File cache put failed for {}", key, ex);
        }
    }

    private File getCacheFile(String key) {
        return getLocalFile(key);
    }

    @Override
    public long size() {
        // sizing the file based cache tree is heavy
        return -1;
    }

    @Override
    public void clear() {
        // do nothing, clear has only sense in memory based cache
    }

    @Override
    public boolean contains(String key) {
        return Files.exists(Paths.get(key));
    }

    public File getLocalFile(String remoteUri)
    {
        URI uri = URI.create(remoteUri);
        StringBuilder sb = new StringBuilder();

        String host = uri.getHost();
        String query = uri.getQuery();
        String path = uri.getPath();

        if (host != null)
        {
            sb.append(host);
        }
        if (path != null)
        {
            sb.append(path);
        }
        if (query != null)
        {
            sb.append('?');
            sb.append(query);
        }

        String name;

        final int maxLen = 250;

        if (sb.length() < maxLen)
        {
            name = sb.toString();
        }
        else
        {
            name = sb.substring(0, maxLen);
        }
        name = normalize(name);

        return new File(baseDir.toFile(), name);
    }

    private static String normalize(String name) {
        name = name.replace('&', '$');
        name = name.replace('?', '$');
        name = name.replace('*', '$');
        name = name.replace(':', '$');
        name = name.replace('<', '$');
        name = name.replace('>', '$');
        name = name.replace('"', '$');
        return name;
    }
}
