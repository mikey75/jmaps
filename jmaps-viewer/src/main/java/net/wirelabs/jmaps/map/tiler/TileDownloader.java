package net.wirelabs.jmaps.map.tiler;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.DummyCache;
import net.wirelabs.jmaps.MapViewer;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.cache.InMemoryTileCache;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Tile downloader - downloads tiles and stores in cache(s)
 */
@Slf4j
public class TileDownloader {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();
    private final InMemoryTileCache inMemoryTileCache = new InMemoryTileCache(8000); // this cache is always on
    private ExecutorService executorService;

    private Cache<String,BufferedImage> localCache = new DummyCache();
    private final MapViewer mapViewer;

    public TileDownloader(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }

    private void download(String tileUrl) {

        if (MapViewer.developerMode) {
            log.info("Getting from: {}", tileUrl);
        }

        Request r = new Request.Builder()
                .url(tileUrl)
                .header("User-Agent", MapViewer.userAgent)
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
            inMemoryTileCache.clear();
        }
        // tile is not loading anymore
        tilesLoading.remove(tileUrl);

    }

    private void readAndCacheImage(String tileUrl, Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            InputStream inputStream = body.byteStream();
            BufferedImage b = ImageIO.read(inputStream);
            if (b != null) {
                inMemoryTileCache.put(tileUrl, b);
                if (localCache.get(tileUrl) == null) {
                    localCache.put(tileUrl, b);
                }
                SwingUtilities.invokeAndWait(() -> {
                    tilesLoading.remove(tileUrl);
                    // emit LOADED event here to ditch the mapviewer reference dependency
                    mapViewer.repaint();
                });

            }
        } catch (InterruptedException | InvocationTargetException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(MapViewer.tilerThreads, new TileDownloaderThreadFactory());
        }
        return executorService;
    }

    public BufferedImage getTile(String url) {

        // check local memory cache
        BufferedImage img = inMemoryTileCache.get(url);
        if (img != null) {
            return inMemoryTileCache.get(url);
        }

        // now check configured local cache

            BufferedImage image = localCache.get(url);
            if (image != null) {
                inMemoryTileCache.put(url, image);
                return inMemoryTileCache.get(url);
            }


        // else submit tile for download from the web
        // but don't submit if it is already submitted
        if (!tilesLoading.contains(url)) {
            tilesLoading.add(url);
            getExecutorService().submit(() -> download(url));
        }

        return null;
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public boolean isTileInCache(String url) {
        return inMemoryTileCache.contains(url);
    }

    public void setImageCacheSize(int size) {
        inMemoryTileCache.newSize(size);
    }

    public void setLocalCache(Cache<String,BufferedImage> localCache) {
        this.localCache = localCache;
    }
}
