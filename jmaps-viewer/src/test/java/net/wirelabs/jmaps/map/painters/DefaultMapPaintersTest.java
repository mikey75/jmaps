package net.wirelabs.jmaps.map.painters;

import net.wirelabs.jmaps.map.MapViewer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.*;
import java.io.File;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class DefaultMapPaintersTest {


    private static Graphics2D graphics;
    private static final MapViewer mapViewer = spy(MapViewer.class);
    private static final BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);

    @BeforeAll
    static void beforeAll() {
        mapViewer.setCurrentMap(new File("src/test/resources/maps/OpenStreetMap.xml"));
        graphics = image.createGraphics();
    }

    @Test
    void testMapAttributionPainter() {
        // given
        MapAttributionPainter attributionPainter = spy(new MapAttributionPainter());
        // when
        attributionPainter.print(graphics, mapViewer, 800, 600);
        // then
        verify(attributionPainter).printText(graphics, 800, 600, "© OpenStreetMap Contributors, Creative Commons Attribution-ShareAlike 2.0");
    }

    @Test
    void testCoordPositionPainter() {
        // given
        CurrentPositionPainter posPainter = spy(new CurrentPositionPainter());
        // when
        posPainter.print(graphics, mapViewer, 800, 600);
        // then
        verify(posPainter).printText(graphics,800,600, "Lon: -180.0000 Lat: 85.0511");

    }
}