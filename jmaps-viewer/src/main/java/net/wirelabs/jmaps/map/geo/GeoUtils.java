package net.wirelabs.jmaps.map.geo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoUtils {

    public static final double MPI = Math.PI;
    public static final double TWO_PI = 2.0 * MPI;
    public static final double HALF_PI = MPI / 2.0;
    public static final double ONE_DEG_IN_RAD = (MPI / 180.0);
    public static final double ONE_RAD_IN_DEG = (180 / MPI);

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

    public static Coordinate calculateCenterOfCoordinateSet(List<Coordinate> route) {

        double minX = route.stream().min(Comparator.comparing(c -> c.longitude)).map(coordinate -> coordinate.longitude).orElse(0.0);
        double maxX = route.stream().max(Comparator.comparing(c -> c.longitude)).map(coordinate -> coordinate.longitude).orElse(0.0);
        double minY = route.stream().min(Comparator.comparing(c -> c.latitude)).map(coordinate ->coordinate.latitude).orElse(0.0);
        double maxY = route.stream().max(Comparator.comparing(c -> c.latitude)).map(coordinate -> coordinate.latitude).orElse(0.0);
        double cx = (maxX - minX) / 2.0;
        double cy = (maxY - minY) / 2.0;

        return new Coordinate(minX + cx, minY + cy);
    }
}

