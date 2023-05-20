package net.wirelabs.jmaps.viewer.geo;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class ProjectionEngineTest {


    // test coordinate (Lublin, PL), expected coordinates taken from epsg.io map
    private final Coordinate gpsCoord = new Coordinate(22.565628, 51.247717);
    private final Coordinate epsg2180Coord = new Coordinate(748754.6993614006, 381708.436327151);
    private final Coordinate epsg3857Coord = new Coordinate(2511994.2245748527, 6665229.511465471);

    @Test
    void testProjectionEngine() {
        testProjection("EPSG:2180",gpsCoord,epsg2180Coord);
        testProjection("EPSG:3857",gpsCoord,epsg3857Coord);
        testInverseProjection("EPSG:2180",epsg2180Coord, gpsCoord);
        testInverseProjection("EPSG:3857",epsg3857Coord, gpsCoord);
    }


    void testProjection(String crs, Coordinate srcCoord, Coordinate expectedCoord) {

        // given
        ProjectionEngine projectionEngine = new ProjectionEngine(crs);
        // when
        Coordinate projectedCoord = projectionEngine.project(srcCoord);
        // then
        assertCorrectProjection(expectedCoord, projectedCoord);
    }

    void testInverseProjection(String crs, Coordinate srcCoord, Coordinate expectedCoord) {
        ProjectionEngine projectionEngine = new ProjectionEngine(crs);
        // when
        Coordinate projectedCoord = projectionEngine.unproject(srcCoord);
        // then
        assertCorrectProjection(expectedCoord, projectedCoord);
    }

    static void assertCorrectProjection(Coordinate expectedCoord, Coordinate projectedCoord) {
        assertThat(projectedCoord.longitude).isEqualTo(expectedCoord.longitude, Offset.offset(0.01));
        assertThat(projectedCoord.latitude).isEqualTo(expectedCoord.latitude, Offset.offset(0.01));
    }



}

