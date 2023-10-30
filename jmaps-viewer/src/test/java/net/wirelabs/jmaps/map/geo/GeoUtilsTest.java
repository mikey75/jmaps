package net.wirelabs.jmaps.map.geo;

import org.junit.jupiter.api.Test;

import static net.wirelabs.jmaps.TestUtils.roundDouble;
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
        assertThat(roundDouble(GeoUtils.deg2rad(1), 7)).isEqualTo(0.0174533);

    }

    @Test
    void testRad2Deg() {
        assertThat(roundDouble(GeoUtils.rad2deg(0.0174533), 6)).isEqualTo(1.000000);
    }
}