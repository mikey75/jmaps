package net.wirelabs.jmaps.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wirelabs.jmaps.map.MapDefinition;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapXMLReader {
    public static MapDefinition parse(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapDefinition.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (MapDefinition) jaxb.unmarshal(file);
    }
}
