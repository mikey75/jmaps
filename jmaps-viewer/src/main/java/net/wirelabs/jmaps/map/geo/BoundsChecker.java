package net.wirelabs.jmaps.map.geo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.cache.BoundsCache;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Slf4j
public class BoundsChecker {
    // defaults used when noarg constructor called
    private final BoundsCache cache;
    private final String epsgIoHost;
    private static final String DOT_JSON = ".json";

    public BoundsChecker() {
        this.epsgIoHost = Defaults.DEFAULT_EPSG_HOST;
        this.cache = new BoundsCache();
    }
    // specify custom epsg host and custom cache
    public BoundsChecker(String epsgIoHost, Path cacheDir) {
        this.epsgIoHost = epsgIoHost;
        this.cache = new BoundsCache(cacheDir);
    }


    private final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public boolean isTrackOutOfBand(List<Coordinate> coords, String epsgString)  {

        int epsgCode = Integer.parseInt(epsgString.substring(epsgString.indexOf(':') + 1));

        if (cache.get(epsgCode + DOT_JSON).isEmpty()) {
            URI uri = URI.create(epsgIoHost + epsgCode + DOT_JSON);
            log.info("Getting bounds from {}", epsgIoHost);

            HttpRequest request = HttpRequest.newBuilder(uri).GET().timeout(Duration.ofSeconds(10)).build();
            HttpResponse<String> response;

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                log.warn("Exception while making request to {}", epsgIoHost, e);
                log.warn("Assuming not out of band");
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            if (response.statusCode() != 200) {
                log.warn("{} http call failed. Assuming not out of band", epsgIoHost);
                return false;
            }
            cache.put(epsgCode+ DOT_JSON, response.body());
        }


        log.info("getting bounds from cache");
        JsonObject root = JsonParser.parseString(cache.get(epsgCode + DOT_JSON)).getAsJsonObject();
        JsonObject bbox = root.getAsJsonObject("bbox");
        if (bbox == null) {
            log.warn("bbox is not specified. Assuming not out of band");
            return false;
        }

        double minLon = bbox.get("west_longitude").getAsDouble();
        double maxLon = bbox.get("east_longitude").getAsDouble();
        double minLat = bbox.get("south_latitude").getAsDouble();
        double maxLat = bbox.get("north_latitude").getAsDouble();

        return coords.stream().anyMatch(coord ->
                coord.getLongitude() < minLon ||
                        coord.getLongitude() > maxLon ||
                        coord.getLatitude() < minLat ||
                        coord.getLatitude() > maxLat
        );

    }
}
