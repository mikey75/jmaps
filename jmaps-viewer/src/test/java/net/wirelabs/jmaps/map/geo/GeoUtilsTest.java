package net.wirelabs.jmaps.map.geo;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 6/3/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class GeoUtilsTest {

    String crs1 = "urn:ogc:def:crs:EPSG::1111";
    String crs2 = "urn:ogc:def:crs:ESRI:102421";
    String crs3 = "urn:ogc:def:crs:OGC:1.3:CRS84";

    @Test
    void testCrsUrn() {
        String a1 = GeoUtils.parseCrsUrn(crs1);
        String a2 = GeoUtils.parseCrsUrn(crs2);
        String a3 = GeoUtils.parseCrsUrn(crs3);

        assertThat(a1).isEqualTo("EPSG:1111");
        assertThat(a2).isEqualTo("ESRI:102421");
        assertThat(a3).isEqualTo("OGC:CRS84");
    }

    @Test
    void testDeg2rad() {
        assertThat(GeoUtils.deg2rad(90)).isEqualTo(90 * GeoUtils.ONE_DEG_IN_RAD);

    }

    @Test
    void testRad2Deg() {
        assertThat(GeoUtils.rad2deg(90 * GeoUtils.ONE_DEG_IN_RAD)).isEqualTo(90);
    }

    @Test
    void shouldCalculateCenterOfCoordinateSet() {

        // suppose a square of
        // topLeft = (0,4) topRight = (4,4)
        // bottomLeft = (0,0) bottomRight= (4,0)

        Coordinate topLeft = new Coordinate(0,4);
        Coordinate topRight = new Coordinate(4,4);
        Coordinate bottomLeft = new Coordinate(0,0);
        Coordinate bottomRight = new Coordinate(4, 0);

        // so the center should be (bottomrx - bottomlx)/2 (toprighty - bottomrighty)/2
        // which is (4-0)/2, (4-0)/2 => (2,2)

        Coordinate calculatedCenter = GeoUtils.calculateCenterOfCoordinateSet(List.of(topLeft, topRight,bottomLeft,bottomRight));
        assertThat(calculatedCenter.getLongitude()).isEqualTo(2.0);
        assertThat(calculatedCenter.getLatitude()).isEqualTo(2.0);

        // now suppose a triangle fit in that square
        // A =0,0 , B=4.0, C = 2.4
        Coordinate A = new Coordinate(0,0);
        Coordinate B = new Coordinate(4,0);
        Coordinate C = new Coordinate(2,4);

        // the center should also be (2,2) since the method calculates the center of the square
        // which contain the given coords
        calculatedCenter = GeoUtils.calculateCenterOfCoordinateSet(List.of(A,B,C));
        assertThat(calculatedCenter.getLongitude()).isEqualTo(2.0);
        assertThat(calculatedCenter.getLatitude()).isEqualTo(2.0);

    }
}