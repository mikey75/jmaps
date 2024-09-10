package net.wirelabs.jmaps.map.readers;

import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.model.map.MapDocument;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class MapReaderTest {

    private static final File MAPFILE = new File("src/test/resources/map.xml");
    private static final File MAPFILE_BAD = new File("src/test/resources/map-bad.xml");
    private static final File MAPFILE_NONEXISTING = new File("nonexisting");


    @Test
    void shouldNotLoadNonExistingMapDefinitionFile() {

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> MapReader.loadMapDefinitionFile(MAPFILE_NONEXISTING))
                .withMessageContaining("Could not load map definition");
    }


    @Test
    void shouldLoadMapDefinitionFile() {
        MapDocument.Map m = MapReader.loadMapDefinitionFile(MAPFILE).getMap();

        assertThat(m.getName()).isEqualTo("Mapa 1");
        assertThat(m.getLayerList()).hasSize(1);
        assertThat(m.getLayerList().get(0).getUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(m.getLayerList().get(0).getType()).isEqualTo(String.valueOf(LayerType.XYZ));
        assertThat(m.getLayerList().get(0).getMinZoom()).isEqualTo(3);


    }

    @Test
    void shouldNotLoadBadMapDefinitionFile() {

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> MapReader.loadMapDefinitionFile(MAPFILE_BAD))
                .withMessageContaining("Could not load map definition");

    }


}
