package net.wirelabs.jmaps.map.downloader;


import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.MapViewer;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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

    private final HttpClient httpClient; 
    private final List<String> tilesLoading = new CopyOnWriteArrayList<>();

    private final MapViewer mapViewer;
    private ExecutorService executorService;


    public DownloadingTileProvider(MapViewer mapViewer) {

        this.mapViewer = mapViewer;
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    private ExecutorService getExecutorService() {

        if (executorService == null) {
            executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileProviderThreadFactory());
        }
        return executorService;
    }

    void download(String tileUrl) {
        log.debug("Getting from: {}", tileUrl);

        HttpRequest r = HttpRequest.newBuilder() //Request.nBuilder()
                .uri(URI.create(tileUrl))
                .header("User-Agent", mapViewer.getUserAgent())
                .build();

        try {

            HttpResponse<InputStream> response = httpClient.send(r, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                readAndCacheImage(tileUrl, response);
            }

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

    private void readAndCacheImage(String tileUrl, HttpResponse<InputStream> response) throws IOException {

            InputStream inputStream = response.body();
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

    public BufferedImage getTile(String url) {

        // check local memory cache
        Optional<BufferedImage> img = Optional.ofNullable(mapViewer.getPrimaryTileCache().get(url));
        if (img.isPresent()) {
            return img.get();
        }

        // now check configured local cache - if the image is there, and it's cache validity is not expired - return it
        if (secondaryCacheEnabled()) {
                Optional<BufferedImage> image = Optional.ofNullable(mapViewer.getSecondaryTileCache().get(url));
                if (image.isPresent() && !mapViewer.getSecondaryTileCache().keyExpired(url)) {
                    mapViewer.getPrimaryTileCache().put(url, image.get());
                    return image.get();
                }


        }

        // else submit tile for download from the web, but only if it's not already submitted
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
