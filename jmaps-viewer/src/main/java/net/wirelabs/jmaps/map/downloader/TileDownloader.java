package net.wirelabs.jmaps.map.downloader;


import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Tile downloader - downloads tiles and stores in cache(s)
 */
@Slf4j
public class TileDownloader {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();

    // primary LRU in memory cache
    final ConcurrentLinkedHashMap<String, BufferedImage> primaryTileCache;
    // secondary cache - default no-cache
    private Cache<String, BufferedImage> secondaryTileCache;

    private final MapViewer mapViewer;
    private ExecutorService executorService;


    public TileDownloader(MapViewer mapViewer) {

        this.mapViewer = mapViewer;
        this.primaryTileCache = new ConcurrentLinkedHashMap.Builder<String, BufferedImage>()
                .maximumWeightedCapacity(Defaults.DEFAULT_IMGCACHE_SIZE)
                .build();
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileDownloaderThreadFactory());
        }
        return executorService;
    }

    void download(String tileUrl) {
        log.debug("Getting from: {}", tileUrl);

        Request r = new Request.Builder()
                .url(tileUrl)
                .header("User-Agent", mapViewer.getUserAgent())
                .get().build();

        try {

            Response response = httpClient.newCall(r).execute();

            if (response.isSuccessful()) {
                readAndCacheImage(tileUrl, response);
            }
            response.body().close();
        } catch (IOException e) {
            log.debug(e.getMessage());
        } catch (OutOfMemoryError e) {
            log.error("DANG! Local memory cache run out of memory");
            log.error("Prunning memory cache...");
            primaryTileCache.clear();
        }
        // tile is not loading anymore
        tilesLoading.remove(tileUrl);

    }

    private void readAndCacheImage(String tileUrl, Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            InputStream inputStream = body.byteStream();
            Optional<BufferedImage> image = Optional.ofNullable(ImageIO.read(inputStream));
            if (image.isPresent()) {
                primaryTileCache.put(tileUrl, image.get());
                if (secondaryCacheEnabled()) {
                    secondaryTileCache.put(tileUrl, image.get());
                }
                tilesLoading.remove(tileUrl);
                // emit LOADED event here to ditch the mapviewer reference dependency
                mapViewer.repaint();
            }
        }
    }

    public BufferedImage getTile(String url) {

        // check local memory cache
        Optional<BufferedImage> img = Optional.ofNullable(primaryTileCache.get(url));
        if (img.isPresent()) {
            return img.get();
        }

        // now check configured local cache
        if (secondaryCacheEnabled()) {
            Optional<BufferedImage> image = Optional.ofNullable(secondaryTileCache.get(url));
            if (image.isPresent()) {
                primaryTileCache.put(url, image.get());
                return image.get();
            }
        }

        // else submit tile for download from the web
        // but don't submit if it is already submitted
        if (!tilesLoading.contains(url)) {
            tilesLoading.add(url);
            getExecutorService().submit(() -> download(url));
        }

        return null;
    }

    private boolean secondaryCacheEnabled() {
        return secondaryTileCache != null;
    }

    public void setImageCacheSize(long size) {
        primaryTileCache.setCapacity(size);
    }

    public void setSecondaryTileCache(Cache<String, BufferedImage> secondaryTileCache) {
        this.secondaryTileCache = secondaryTileCache;
    }
}
