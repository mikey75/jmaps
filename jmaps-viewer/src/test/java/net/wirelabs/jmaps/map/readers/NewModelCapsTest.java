package net.wirelabs.jmaps.map.readers;

import net.opengis.wmts.x10.CapabilitiesDocument;
import org.apache.xmlbeans.XmlException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class NewModelCapsTest {
    private File testFile1 = new File("src/test/resources/wmts/capabilities.xml");
    private File testFile2 = new File("src/test/resources/wmts/capabilities-2.xml");

    private static CapabilitiesDocument.Capabilities parseCapabilitiesFromFile(File capabilitiesFile) throws XmlException, IOException {
        CapabilitiesDocument cap = CapabilitiesDocument.Factory.parse(capabilitiesFile);
        return cap.getCapabilities();

    }

    @Test
    void shouldParse() throws XmlException, IOException {
        parseCapabilitiesFromFile(testFile2);
    }

}
