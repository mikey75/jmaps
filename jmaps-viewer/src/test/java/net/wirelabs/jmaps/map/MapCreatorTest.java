package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class MapCreatorTest {

    private final File testMapsDir = new File("src/test/resources/map-creator/");
    private final File CORRECT_SINGLELAYER = new File(testMapsDir, "correct-singlelayer.xml");
    private final File CORRECT_MULTILATER = new File(testMapsDir, "correct-multilayer.xml");

    private final File TWO_LAYERS_NAME_COLLISION = new File(testMapsDir, "multilayer-samelayer.xml");
    private final File UNKNOWN_LAYER_TYPE = new File(testMapsDir, "layer-unsupported.xml");
    private final File TWO_LAYERS_DIFFERENT_TILESIZE = new File(testMapsDir, "multilayer-different-tilesize.xml");
    private final File TWO_LAYERS_DIFFERENT_CRS = new File(testMapsDir,"multilayer-different-crs.xml");

    @Test
    void testCorrectMaps()  {

        MapCreator mc = new MapCreator();

        MapObject map = mc.createMap(CORRECT_SINGLELAYER);
        assertMapObject(map,true, 1 , "Map1");

        map = mc.createMap(CORRECT_MULTILATER);
        assertMapObject(map,true, 2, "Map1");

    }

    @Test
    void testIncorrectMaps() {
        MapCreator mc = new MapCreator();
        MapObject map;

        // two layers with the same name
        map = mc.createMap(TWO_LAYERS_NAME_COLLISION);
        // only one layer created
        assertMapObject(map, true, 1,"Map1");

        // non-matching tilesize on two layers
        map = mc.createMap(TWO_LAYERS_DIFFERENT_TILESIZE);
        // only one layer created
        assertMapObject(map, true, 1, "Map1");

        // non-matching crs
        map = mc.createMap(TWO_LAYERS_DIFFERENT_CRS);
        assertMapObject(map, true, 1, "Map1");


        // unsupported layer throws exception
        assertThatThrownBy(() -> mc.createMap(UNKNOWN_LAYER_TYPE))
                .isInstanceOf(CriticalMapException.class)
                .hasMessage("Layer type not given or unknown");


    }


    private void assertMapObject(MapObject map, boolean layersPresent, int layerCount, String mapName) {
        assertThat(map.layersPresent()).isEqualTo(layersPresent);
        assertThat(map.getLayers()).hasSize(layerCount);
        assertThat(map.getMapName()).isEqualTo(mapName);
    }
}