package net.wirelabs.jmaps.map.readers;

import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
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
    private static final MapReader mapReader = new MapReader();

    @Test
    void shouldNotLoadNonExistingMapDefinitionFile() {

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> mapReader.loadMapDefinitionFile(MAPFILE_NONEXISTING))
                .withMessageMatching("Could not load map definition");
    }


    @Test
    void shouldLoadMapDefinitionFile() throws JAXBException {
        MapDefinition m = mapReader.loadMapDefinitionFile(MAPFILE);

        assertThat(m.getName()).isEqualTo("Mapa 1");
        assertThat(m.getLayers()).hasSize(1);
        assertThat(m.getLayers().get(0).getUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(m.getLayers().get(0).getType()).isEqualTo(LayerType.XYZ);
        assertThat(m.getLayers().get(0).getMinZoom()).isEqualTo(3);
        assertThat(m.getLayers().get(0).getOpacity()).isEqualTo(1.0f);

    }

    @Test
    void shouldNotLoadBadMapDefinitionFile() {

        assertThatExceptionOfType(CriticalMapException.class)
                .isThrownBy(() -> mapReader.loadMapDefinitionFile(MAPFILE_BAD))
                .withMessageContaining("Could not load map definition");

    }


}
