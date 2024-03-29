package net.wirelabs.jmaps.map.geo;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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

    /**
     * Calculate enclosing rectangle such that all coordinate points fit inside it
     * The resulting rectangle is in screen pixels, not world coordinates
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
     * @param coordinates set of coordinates
     * @return lat/lon of the center point
     */
    public static Coordinate calculateCenterOfCoordinateSet(List<Coordinate> coordinates) {
        // calculate enclosing rectangle for all coordinates
        Rectangle2D r2 = calculateEnclosingRectangle(coordinates);
        // return center of that rectangle
        return new Coordinate(r2.getCenterX(), r2.getCenterY());
    }

}

