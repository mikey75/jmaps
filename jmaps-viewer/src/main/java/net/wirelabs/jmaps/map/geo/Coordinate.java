package net.wirelabs.jmaps.map.geo;

import lombok.Getter;

/**
 * Created 5/22/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Class represents map coordinate used throughout the jMaps library
 * <p>
 * It can be a geographic position - in lon/lat/elevation
 * or projected position (in crs units)
 * <p>
 * To avoid conversion headaches, both (geo position and crs units)
 * are stored in geographical namespace i.e lon/lat/elevation.
 */

@Getter
public class Coordinate {

    private final double longitude;     // x
    private final double latitude;      // y
    private final double altitude;      // z

    public Coordinate(double longitude, double latitude, double altitude) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;

    }

    public Coordinate(double longitude, double latitude) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = 0.0d;

    }

}
