package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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


}

