package net.wirelabs.jmaps.map.downloader;


import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.MapViewer;
import okhttp3.*;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Tile downloader - downloads tiles and stores in cache(s)
 */
@Slf4j
public class DownloadingTileProvider implements TileProvider {

    private final OkHttpClient httpClient = new OkHttpClient();
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();

    private final MapViewer mapViewer;
    private ExecutorService executorService;


    public DownloadingTileProvider(MapViewer mapViewer) {

        this.mapViewer = mapViewer;
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileProviderThreadFactory());
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
            response.close();
        } catch (Exception e) {
            log.debug("Could not download {} - {} : {}", tileUrl, e.getClass().getSimpleName() ,e.getMessage());
        } catch (OutOfMemoryError e) {
            log.error("DANG! Local memory cache run out of memory");
            log.error("Prunning memory cache...");
            mapViewer.getPrimaryTileCache().clear();
        }
        // tile is not loading anymore
        tilesLoading.remove(tileUrl);

    }

    private void readAndCacheImage(String tileUrl, Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            InputStream inputStream = body.byteStream();
            Optional<BufferedImage> image = Optional.ofNullable(ImageIO.read(inputStream));
            if (image.isPresent()) {
                mapViewer.getPrimaryTileCache().put(tileUrl, image.get());
                if (secondaryCacheEnabled()) {
                    mapViewer.getSecondaryTileCache().put(tileUrl, image.get());
                }
                tilesLoading.remove(tileUrl);
                // emit LOADED event here to ditch the mapviewer reference dependency
                mapViewer.repaint();
            }
        }
    }

    public BufferedImage getTile(String url) {

        // check local memory cache
        Optional<BufferedImage> img = Optional.ofNullable(mapViewer.getPrimaryTileCache().get(url));
        if (img.isPresent()) {
            return img.get();
        }

        // now check configured local cache
        if (secondaryCacheEnabled()) {
                Optional<BufferedImage> image = Optional.ofNullable(mapViewer.getSecondaryTileCache().get(url));
                if (image.isPresent()) {
                    mapViewer.getPrimaryTileCache().put(url, image.get());
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
        return mapViewer.getSecondaryTileCache() != null;
    }
}
