package net.wirelabs.jmaps.utils;

import net.wirelabs.jmaps.viewer.geo.GeoUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static net.wirelabs.jmaps.viewer.geo.GeoUtils.deg2rad;
import static net.wirelabs.jmaps.viewer.geo.GeoUtils.rad2deg;
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
        Assertions.assertThat(GeoUtils.deg2rad(1)).isEqualTo(0.0174533, Offset.offset(0.001));

    }
    @Test
    void testRad2Deg() {
        Assertions.assertThat(GeoUtils.rad2deg(0.0174533)).isEqualTo(1,Offset.offset(0.001));
    }
}