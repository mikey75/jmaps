package net.wirelabs.jmaps.map.readers;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.map.model.map.MapDefinition;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Map reader - handles reading map definition xml
 * and wmts capabilities xml
 * <p>
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j

public class MapReader {

    public  MapDefinition loadMapDefinitionFile(File mapDefinitionFile) {
        try {
            JAXBContext context = JAXBContext.newInstance(MapDefinition.class);
            Unmarshaller jaxb = context.createUnmarshaller();
            return (MapDefinition) jaxb.unmarshal(mapDefinitionFile);
        } catch (Exception e) {
            throw new CriticalMapException("Could not load map definition", e);
        }
    }
}
