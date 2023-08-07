package net.wirelabs.jmaps.utils;

import net.wirelabs.jmaps.map.MapDefinition;

import net.wirelabs.jmaps.map.layer.LayerType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBException;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class MapXMLReaderTest {

    File mapfile = new File("src/test/resources/map.xml");

    @Test
    void test() throws JAXBException {

        MapDefinition m = MapXMLReader.parse(mapfile);
        Assertions.assertThat(m.getName()).isEqualTo("Mapa 1");
        Assertions.assertThat(m.getLayers()).hasSize(1);
        Assertions.assertThat(m.getLayers().get(0).getUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");
        Assertions.assertThat(m.getLayers().get(0).getType()).isEqualTo(LayerType.XYZ);
        Assertions.assertThat(m.getLayers().get(0).getMinZoom()).isEqualTo(3);
        Assertions.assertThat(m.getLayers().get(0).getOpacity()).isEqualTo(1.0f);

    }


}