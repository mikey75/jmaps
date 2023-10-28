package net.wirelabs.jmaps.map.geo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class ProjectionEngineTest {


    // test coordinate (Lublin, PL), expected coordinates taken from epsg.io map
    private static final Coordinate GPS_COORDINATE_LUBLIN = new Coordinate(22.565628, 51.247717);
    private static final Coordinate EPSG_2180_COORD_LUBLIN = new Coordinate(748754.6993614006, 381708.436327151);
    private static final Coordinate EPSG_3857_COORD_LUBLIN = new Coordinate(2511994.2245748527, 6665229.511465471);

    @Test
    void testProjectionEngine() {
        testProjection("EPSG:2180", EPSG_2180_COORD_LUBLIN);
        testProjection("EPSG:3857", EPSG_3857_COORD_LUBLIN);
        testInverseProjection("EPSG:2180", EPSG_2180_COORD_LUBLIN);
        testInverseProjection("EPSG:3857", EPSG_3857_COORD_LUBLIN);
    }


    void testProjection(String crs, Coordinate expectedCoord) {

        // given
        ProjectionEngine projectionEngine = new ProjectionEngine(crs);
        // when
        Coordinate projectedCoord = projectionEngine.project(GPS_COORDINATE_LUBLIN);
        // then
        assertCorrectProjection(expectedCoord, projectedCoord);
    }

    void testInverseProjection(String crs, Coordinate srcCoord) {
        ProjectionEngine projectionEngine = new ProjectionEngine(crs);
        // when
        Coordinate projectedCoord = projectionEngine.unproject(srcCoord);
        // then
        assertCorrectProjection(GPS_COORDINATE_LUBLIN, projectedCoord);
    }

    static void assertCorrectProjection(Coordinate expectedCoord, Coordinate projectedCoord) {
        assertThat(projectedCoord.getLongitude()).isEqualTo(expectedCoord.getLongitude(), within(0.01));
        assertThat(projectedCoord.getLatitude()).isEqualTo(expectedCoord.getLatitude(), within(0.01));
    }

}

