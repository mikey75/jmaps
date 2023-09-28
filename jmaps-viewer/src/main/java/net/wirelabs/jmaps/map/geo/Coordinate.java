package net.wirelabs.jmaps.map.geo;

import lombok.NoArgsConstructor;
import net.wirelabs.jmaps.map.utils.NumberUtils;


import static java.lang.Double.NaN;

/**
 * Created 5/22/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Class represents map coordinate used throughout the jmaps library
 * <p>
 * It can be a geographic position - in lon/lat/elevation
 * or projected position (in crs units)
 * <p>
 * To avoid conversion headaches, all values are in geographical namespace
 * i.e lon/lat/elevation.
 */

@NoArgsConstructor
public class Coordinate {

    public double longitude;     // x
    public double latitude;      // y
    public double altitude;     // z

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


    public Coordinate roundTo(int decimalPlace) {

        longitude = NumberUtils.roundDouble(longitude, decimalPlace);
        latitude = NumberUtils.roundDouble(latitude, decimalPlace);
        altitude = NumberUtils.roundDouble(altitude, decimalPlace);
        return this;
    }

}
