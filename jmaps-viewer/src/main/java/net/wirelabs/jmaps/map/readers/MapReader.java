package net.wirelabs.jmaps.map.readers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import net.wirelabs.jmaps.model.map.MapDocument;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

/**
 * Map reader - handles reading map definition xml
 * and wmts capabilities xml
 * <p>
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapReader {

    public  static MapDocument loadMapDefinitionFile(File mapDefinitionFile) {
        try {
            return MapDocument.Factory.parse(mapDefinitionFile);
        } catch (IOException | XmlException e) {
            log.error("Could not load map definition: {}", e.getMessage());
            throw new CriticalMapException("Could not load map definition!\nDetails: check logs");
        }
    }
}
