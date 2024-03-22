package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.model.map.LayerDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.stream.Stream;

import static net.wirelabs.jmaps.TestUtils.roundDouble;
import static org.assertj.core.api.Assertions.assertThat;

class XYZLayerTest {

    private LayerDefinition xyzLayerDefinition;



    @BeforeEach
    void before() {
        xyzLayerDefinition = new LayerDefinition();
        xyzLayerDefinition.setType(LayerType.XYZ);
        xyzLayerDefinition.setName("TestXYZ");
        xyzLayerDefinition.setUrl("http://localhost/{z}/{x}/{y}.png");
    }

    @Test
    void shouldCreateCorrectUrl() {
        Layer xyz = new XYZLayer(xyzLayerDefinition);
        assertThat(xyz.createTileUrl(4,5,12)).isEqualTo("http://localhost/12/4/5.png");
    }


    @ParameterizedTest
    @MethodSource("providePoints")
    void shouldConvertToPixelAndBack(Coordinate coord) {

        Layer xyz = new XYZLayer(xyzLayerDefinition);
        //Coordinate lublin = new Coordinate(22.4900397, 51.2326363);

        Point2D pixel = xyz.latLonToPixel(coord, 3);
        Coordinate pixelConvertedBackToLatLon = xyz.pixelToLatLon(pixel, 3);

        assertThat(roundDouble(pixelConvertedBackToLatLon.getLongitude(), 7)).isEqualTo(coord.getLongitude());
        assertThat(roundDouble(pixelConvertedBackToLatLon.getLatitude(), 7)).isEqualTo(coord.getLatitude());



    }

    public static Stream<Arguments> providePoints() {

        return Stream.of(

                Arguments.of(new Coordinate(22.4900397, 51.2326363)),
                Arguments.of(new Coordinate( -45.490, 43.213)),
                Arguments.of(new Coordinate(39.091, -84.01)),
                Arguments.of(new Coordinate(-39.091, -84.01))
        );


    }

    @Test
    void getTopLeftCorner() {
        Layer xyz = new XYZLayer(xyzLayerDefinition);
        Point2D corner = xyz.getTopLeftCornerInMeters();
        assertThat(corner.getX()).isEqualTo(-xyz.getProjectionEngine().getEquatorLength() /2 );
        assertThat(corner.getY()).isEqualTo(xyz.getProjectionEngine().getPolarLength() /2 );
    }

    @Test
    void getMapSizeInPixels() {
        Layer xyz = new XYZLayer(xyzLayerDefinition);

        Dimension d = xyz.getSizeInTiles(2);
        // at zoom 2 there are 2^2 = 4 tiles
        // so (4*256),(4*256) is map size in pixels
        assertThat(d.height).isEqualTo(4);
        assertThat(d.width).isEqualTo(4);

    }

    @Test
    void shouldCreateXYZLayerWithDefaultValues() {

        // xyz layer with defaults
        Layer xyz = new XYZLayer(xyzLayerDefinition);

        assertThat(xyz.getType()).isEqualTo(LayerType.XYZ);
        assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:3857"); // assert default crs, we did not set
        assertThat(xyz.getCrs()).isEqualTo("EPSG:3857");
        assertThat(xyz.getTileSize()).isEqualTo(256);
        assertThat(xyz.getMaxZoom()).isEqualTo(18);
        assertThat(xyz.getMinZoom()).isZero();
        assertThat(xyz.isSwapAxis()).isFalse(); //boolean swapAxis = false;
        assertThat(xyz.getOpacity()).isEqualTo(1.0f);
        assertThat(xyz.getZoomOffset()).isZero();
    }


    @Test
    void shouldCreateXYZLayerWithNonDefaultValues()  {

        // xyz layer with custom params

        xyzLayerDefinition.setCrs("EPSG:2180");
        xyzLayerDefinition.setTileSize(100);
        xyzLayerDefinition.setMaxZoom(10);
        xyzLayerDefinition.setMinZoom(4);
        xyzLayerDefinition.setSwapAxis(true);
        xyzLayerDefinition.setOpacity(0.8f);
        xyzLayerDefinition.setZoomOffset(1);

        Layer xyz = new XYZLayer(xyzLayerDefinition);

        assertThat(xyz.getType()).isEqualTo(LayerType.XYZ);
        assertThat(xyz.getProjectionEngine().getCrs().getName()).isEqualTo("EPSG:2180"); // assert default crs, we did not set
        assertThat(xyz.getCrs()).isEqualTo("EPSG:2180");

        assertThat(xyz.getTileSize()).isEqualTo(100);
        assertThat(xyz.getMaxZoom()).isEqualTo(10);
        assertThat(xyz.getMinZoom()).isEqualTo(4);
        assertThat(xyz.isSwapAxis()).isTrue();
        assertThat(xyz.getOpacity()).isEqualTo(0.8f);
        assertThat(xyz.getZoomOffset()).isEqualTo(1);

    }

}
