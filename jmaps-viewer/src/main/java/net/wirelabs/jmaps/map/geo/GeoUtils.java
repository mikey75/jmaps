package net.wirelabs.jmaps.map.geo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.BoundsCache;

import java.awt.geom.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoUtils {
    @Getter
    private static final BoundsCache cache = new BoundsCache();
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static final double MPI = Math.PI;
    public static final double TWO_PI = 2.0 * MPI;
    public static final double HALF_PI = MPI / 2.0;
    public static final double ONE_DEG_IN_RAD = (MPI / 180.0);
    public static final double ONE_RAD_IN_DEG = (180 / MPI);
    @Getter
    @Setter
    private static String epsgIoHost = "https://epsg.io/";

    public static String parseCrsUrn(String urn) {

        return urn.replace("urn:ogc:def:crs:", "")
                .replaceAll(":.*:", ":");
    }

    public static double rad2deg(double rad) {
        return rad * ONE_RAD_IN_DEG;
    }

    public static double deg2rad(double deg) {
        return deg * ONE_DEG_IN_RAD;
    }

    /**
     * Calculate enclosing rectangle such that all coordinate points fit inside it
     * The resulting rectangle is in screen pixels, not world coordinates
     *
     * @param coordinates list of coordinates (for instance a route, or set of waypoints)
     * @return resulting rectangle
     */
    public static Rectangle2D calculateEnclosingRectangle(List<Coordinate> coordinates) {
        Point2D firstPoint = new Point2D.Double(coordinates.get(0).getLongitude(), coordinates.get(0).getLatitude());
        Rectangle2D r2 = new Rectangle2D.Double(firstPoint.getX(), firstPoint.getY(), 0, 0);

        for (Coordinate c : coordinates) {
            Point2D p = new Point2D.Double(c.getLongitude(), c.getLatitude());
            r2.add(p);
        }
        return r2;
    }

    /**
     * Calculate the geometric center of set of coordinates - in lat/lon units
     *
     * @param coordinates set of coordinates
     * @return lat/lon of the center point
     */
    public static Coordinate calculateCenterOfCoordinateSet(List<Coordinate> coordinates) {
        // calculate enclosing rectangle for all coordinates
        Rectangle2D r2 = calculateEnclosingRectangle(coordinates);
        // return center of that rectangle
        return new Coordinate(r2.getCenterX(), r2.getCenterY());
    }

    public static boolean isTrackOutOfBand(List<Coordinate> coords, String epsgString)  {

        int epsgCode = Integer.parseInt(epsgString.substring(epsgString.indexOf(':') + 1));

        if (cache.get(epsgCode +".json").isEmpty()) {
            log.info("Getting bounds from epsg.io");
            URI uri = URI.create(epsgIoHost + epsgCode + ".json");

            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> response;

            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                log.warn("Exception while making request. Assuming not out of band");
                return false;
            }
            if (response.statusCode() != 200) {
                log.warn("epsg.io http call failed. Assuming not out of band");
                return false;
            }
            cache.put(epsgCode+".json", response.body());
        }


        log.info("getting bounds from cache");
        JsonObject root = JsonParser.parseString(cache.get(epsgCode + ".json")).getAsJsonObject();
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

