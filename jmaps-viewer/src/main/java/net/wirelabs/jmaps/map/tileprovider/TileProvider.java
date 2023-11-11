package net.wirelabs.jmaps.map.tileprovider;


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
public class TileProvider {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();

    // primary LRU in memory cache
    private final ConcurrentLinkedHashMap<String,BufferedImage> primaryTileCache;
    // secondary cache - default none
    private Cache<String, BufferedImage> secondaryTileCache;

    private final MapViewer mapViewer;
    private ExecutorService executorService;


    public TileProvider(MapViewer mapViewer) {

        this.mapViewer = mapViewer;
        this.primaryTileCache = new ConcurrentLinkedHashMap.Builder<String,BufferedImage>()
                .maximumWeightedCapacity(Defaults.DEFAULT_IMGCACHE_SIZE)
                .build();
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileProviderThreadFactory());
        }
        return executorService;
    }

    void download(String tileUrl) {

        log.debug("Downloading tile from: {}", tileUrl);

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
                if (isSecondaryCacheEnabled() && !secondaryTileCache.contains(tileUrl)) {
                    secondaryTileCache.put(tileUrl, image.get());
                }
                tilesLoading.remove(tileUrl);
                // emit LOADED event here to ditch the mapviewer reference dependency
                mapViewer.repaint();

            }
        }
    }

    public BufferedImage getTile(String url) {

        if (primaryTileCache.containsKey(url)) {
            return primaryTileCache.get(url);
        }

        // now check configured local cache
        // if tile exists and is not null - write it thru to primary, return image
        if (isSecondaryCacheEnabled() && secondaryTileCache.contains(url)) {
            Optional<BufferedImage> image = Optional.ofNullable(secondaryTileCache.get(url));
            image.ifPresent(i -> primaryTileCache.put(url, image.get()));
            return primaryTileCache.get(url);
        }

        // else submit tile for download from the web
        // but don't submit if it is already submitted
        if (!tilesLoading.contains(url)) {
            tilesLoading.add(url);
            getExecutorService().submit(() -> download(url));
        }

        return null;
    }

    public boolean isTileInCache(String url) {
        return primaryTileCache.containsKey(url);
    }

    public void setImageCacheSize(long size) {
        primaryTileCache.setCapacity(size);
    }

    public void setSecondaryTileCache(Cache<String, BufferedImage> secondaryTileCache) {
        this.secondaryTileCache = secondaryTileCache;
    }

    private boolean isSecondaryCacheEnabled() {
        return secondaryTileCache != null;
    }

}
