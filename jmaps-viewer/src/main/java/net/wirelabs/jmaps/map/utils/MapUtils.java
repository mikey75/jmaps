package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wirelabs.jmaps.map.geo.Coordinate;

import java.util.Comparator;
import java.util.List;

/**
 * Created 9/27/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapUtils {

    public static Coordinate calculateCenterOfCoordinateSet(List<Coordinate> route) {

        double minX = route.stream().min(Comparator.comparing(c -> c.longitude)).get().longitude;
        double maxX = route.stream().max(Comparator.comparing(c -> c.longitude)).get().longitude;
        double minY = route.stream().min(Comparator.comparing(c -> c.latitude)).get().latitude;
        double maxY = route.stream().max(Comparator.comparing(c -> c.latitude)).get().latitude;
        double cx = (maxX - minX) / 2.0;
        double cy = (maxY - minY) / 2.0;

        return new Coordinate(minX + cx, minY + cy);
    }
}
