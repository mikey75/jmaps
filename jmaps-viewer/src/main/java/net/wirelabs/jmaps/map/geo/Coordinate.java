package net.wirelabs.jmaps.map.geo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static java.lang.Double.NaN;

/**
 * Created 5/22/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Class represents map coordinate used throughout the jmaps library
 * <p>
 * It can be a geographic position - in lon/lat/elevation
 * or projected position (in crs units)
 * <p>
 * To avoid conversion headaches, both (geo position and crs units)
 * are stored in geographical namespace i.e lon/lat/elevation.
 */

@NoArgsConstructor
@Getter
@Setter
public class Coordinate {

    private double longitude;     // x
    private double latitude;      // y
    private double altitude;     // z

    public Coordinate(double longitude, double latitude, double altitude) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;

    }

    public Coordinate(double longitude, double latitude) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = NaN;

    }

}
