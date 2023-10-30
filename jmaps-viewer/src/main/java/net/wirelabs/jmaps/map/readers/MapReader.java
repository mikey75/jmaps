package net.wirelabs.jmaps.map.readers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Map reader - handles reading map definition xml
 * and wmts capabilities xml
 * <p>
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapReader {

    public static MapDefinition loadMapDefinitionFile(File mapDefinitionFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapDefinition.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (MapDefinition) jaxb.unmarshal(mapDefinitionFile);
    }
}
