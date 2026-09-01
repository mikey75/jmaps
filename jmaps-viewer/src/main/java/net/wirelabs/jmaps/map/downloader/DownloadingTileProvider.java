package net.wirelabs.jmaps.map.downloader;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.MapViewer;
import net.wirelabs.jmaps.map.cache.memory.InMemoryLRUCache;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Tile downloader - downloads tiles and stores in cache(s)
 */
@Slf4j
public class DownloadingTileProvider implements TileProvider {

    private final HttpClient httpClient;
    private final Set<String> tilesLoading = ConcurrentHashMap.newKeySet();

    private final MapViewer mapViewer;
    private final ExecutorService executorService;

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);

    // local in-memory cache should be local to provider
    @Getter
    private final InMemoryLRUCache primaryTileCache = new InMemoryLRUCache();

    public DownloadingTileProvider(MapViewer mapViewer, HttpClient httpClient) {
        this.mapViewer = mapViewer;
        this.executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileProviderThreadFactory());
        this.httpClient = httpClient;
    }

    public DownloadingTileProvider(MapViewer mapViewer) {

        this.mapViewer = mapViewer;
        this.executorService = Executors.newFixedThreadPool(mapViewer.getTilerThreads(), new TileProviderThreadFactory());
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
    }

    // package private for test
    void download(String downloadUrl, String cacheUrl) {
        log.debug("Getting from: {}", downloadUrl);

        HttpRequest tileRequest = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .header("User-Agent", Defaults.DEFAULT_USER_AGENT)
                .timeout(REQUEST_TIMEOUT)
                .build();

        try {

            HttpResponse<InputStream> response = httpClient.send(tileRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                readAndCacheImage(cacheUrl, response);
            } else {
                log.debug("Could not download {} - Http response {}", downloadUrl, response.statusCode());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Download interrupted for {}", downloadUrl);
        } catch (IOException e) {
            log.debug("Could not download {} - {} : {}", downloadUrl, e.getClass().getSimpleName(), e.getMessage());
        } finally {
            // tile is not loading anymore
            tilesLoading.remove(downloadUrl);
        }

    }

    private void readAndCacheImage(String cacheUrl, HttpResponse<InputStream> response) {

        try (InputStream responseBody = response.body()) {
            BufferedImage image = ImageIO.read(responseBody);
            if (image != null) {
                primaryTileCache.put(cacheUrl, image);
                if (secondaryTileCacheEnabled()) {
                    mapViewer.getSecondaryTileCache().put(cacheUrl, image);
                }
                mapViewer.repaint();
            } else {
                log.error("Image could not be loaded");
            }
        } catch (IOException e) {
            log.error("IO Exception: {}", e.getMessage());
        }
    }

    public BufferedImage getTile(String url, String cacheUrl) {

        // check local memory cache
        BufferedImage img = primaryTileCache.get(cacheUrl);
        if (img != null) {
            return img;
        }

        // now check configured local (secondary) cache
        // if the image is there, and it's cache validity is not expired - return it
        if (secondaryTileCacheEnabled()) {
                BufferedImage image = mapViewer.getSecondaryTileCache().get(cacheUrl);
                if (image != null && !mapViewer.getSecondaryTileCache().keyExpired(cacheUrl)) {
                    primaryTileCache.put(cacheUrl, image);
                    return image;
                }
        }

        // else schedule tile for download from the web, but only if it's not already submitted
        scheduleDownload(url, cacheUrl);
        return null;
    }

     private void scheduleDownload(String url, String cacheUrl) {
        if (tilesLoading.add(url)) {
            executorService.submit(() -> download(url, cacheUrl));
        }
    }

    private boolean secondaryTileCacheEnabled() {
        return mapViewer.getSecondaryTileCache() != null;
    }
}
