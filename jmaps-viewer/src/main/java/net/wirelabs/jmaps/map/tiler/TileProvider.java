package net.wirelabs.jmaps.map.tiler;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import net.wirelabs.jmaps.MapViewer;
import net.wirelabs.jmaps.map.layer.Layer;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class TileProvider {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();

    // setters for own implementations
    @Getter @Setter private Cache<String, BufferedImage> imageCache;
    @Setter private Cache<String, BufferedImage> localCache;

    private final MapViewer mapViewer;
    private int threadCount;
    private final Layer layer;
    private ExecutorService executorService;


    public TileProvider(MapViewer mapViewer, Layer layer, int threadCount) {
        this.layer = layer;
        this.mapViewer = mapViewer;
        this.threadCount = threadCount;
        this.localCache = new DirectoryBasedCache(mapViewer.getTileCacheDir());
        this.imageCache = new TileCache(mapViewer.getImageCacheSize());
    }

    private void doDownload(String tileUrl) {

        if (mapViewer.isCacheDebug()) {
            log.info("Getting from: {}", tileUrl);
        }

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
            imageCache.clear();
        }
        // tile is not loading anymore
        tilesLoading.remove(tileUrl);

    }

    private void readAndCacheImage(String tileUrl, Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            InputStream inputStream = body.byteStream();
            BufferedImage b = ImageIO.read(inputStream);
            if (b != null) {
                imageCache.put(tileUrl, b);
                if (localCache.get(tileUrl) == null) {
                    localCache.put(tileUrl, b);
                }
                SwingUtilities.invokeAndWait(() -> {
                    tilesLoading.remove(tileUrl);
                    // emit LOADED event here
                    mapViewer.repaint();
                });

            }
        } catch (InterruptedException | InvocationTargetException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {

            executorService = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {

                int threadsSpawned = 0;

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, layer.getName()+ "-Tiler-" + threadsSpawned++);
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.setDaemon(true);
                    return t;
                }
            });
        }
        return executorService;
    }

    public BufferedImage getTile(String url) {

        // check local memory cache
        BufferedImage img = imageCache.get(url);
        if (img != null) {
            return imageCache.get(url);
        }

        // now check configured local cache
        BufferedImage image = localCache.get(url);
        if (image != null) {
            imageCache.put(url, image);
            return imageCache.get(url);
        }

        // else submit tile for download from the web
        // but don't submit if it is already submitted
        if (!tilesLoading.contains(url)) {
            tilesLoading.add(url);
            getExecutorService().submit(() -> doDownload(url));
        }

        return null;
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }

    }

    public boolean isTileInCache(String url) {
        return imageCache.contains(url);
    }
}
