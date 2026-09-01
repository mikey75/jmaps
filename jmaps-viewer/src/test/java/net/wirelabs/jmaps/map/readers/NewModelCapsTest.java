package net.wirelabs.jmaps.map.readers;

import net.opengis.wmts.x10.CapabilitiesDocument;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class NewModelCapsTest {
    private File testFile1 = new File("src/test/resources/wmts/capabilities.xml");
    private File testFile2 = new File("src/test/resources/wmts/capabilities-2.xml");

    private static CapabilitiesDocument.Capabilities parseCapabilitiesFromFile(File capabilitiesFile) throws XmlException, IOException {
        CapabilitiesDocument cap = CapabilitiesDocument.Factory.parse(capabilitiesFile);
        return cap.getCapabilities();

    }

    @Test
    void shouldParse() throws XmlException, IOException {
        CapabilitiesDocument.Capabilities capabilities = parseCapabilitiesFromFile(testFile2);
        // just some basic test of one matrix set. no need to check all if one is parsed ok
        assertThat(capabilities.getContents().getTileMatrixSetList()).hasSize(15);
        assertThat(capabilities.getContents().getTileMatrixSetList().get(1).validate()).isTrue();
        assertThat(capabilities.getContents().getTileMatrixSetList().get(1).getSupportedCRS()).isEqualTo("EPSG:3035");
    }

}
