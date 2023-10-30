package net.wirelabs.jmaps.map.readers;

import net.wirelabs.jmaps.map.layer.LayerType;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
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

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> MapReader.loadMapDefinitionFile(MAPFILE_NONEXISTING))
                .withMessageMatching("(.*No such file or directory.*|.*The system cannot find the file specified.*)");
    }


    @Test
    void shouldLoadMapDefinitionFile() throws JAXBException {

        // MapReader mapReader = new MapReader();

        MapDefinition m = MapReader.loadMapDefinitionFile(MAPFILE);

        assertThat(m.getName()).isEqualTo("Mapa 1");
        assertThat(m.getLayers()).hasSize(1);
        assertThat(m.getLayers().get(0).getUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(m.getLayers().get(0).getType()).isEqualTo(LayerType.XYZ);
        assertThat(m.getLayers().get(0).getMinZoom()).isEqualTo(3);
        assertThat(m.getLayers().get(0).getOpacity()).isEqualTo(1.0f);

    }

    @Test
    void shouldNotLoadBadMapDefinitionFile() {

        assertThatExceptionOfType(UnmarshalException.class)
                .isThrownBy(() -> MapReader.loadMapDefinitionFile(MAPFILE_BAD))
                .withMessageContaining("unexpected element");

    }


}
