package net.wirelabs.jmaps.map;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.model.map.MapDefinition;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.wirelabs.jmaps.map.Defaults.DEFAULT_WMTS_DESCRIPTOR_CACHE;

/**
 * Map reader - handles reading map definition xml
 * and wmts capabilities xml
 * <p>
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class MapReader {

    private final String wmtsCacheDir;

    public MapReader(String wmtsCacheDir) {
        this.wmtsCacheDir = wmtsCacheDir;
    }

    public MapReader() {
        this(DEFAULT_WMTS_DESCRIPTOR_CACHE);
    }

    public Capabilities loadCapabilities(String getCapabilitiesUrl) {

        try {

            URI uri = URI.create(getCapabilitiesUrl);
            File cachedFile = Paths.get(wmtsCacheDir,uri.getHost(), uri.getPath(), "capabilities.xml").toFile();

            if (!cachedFile.exists()) {
                log.info("Loading WMTS capabilities from {}", getCapabilitiesUrl);
                URL request = new URL(getCapabilitiesUrl);
                InputStream is = request.openConnection().getInputStream();
                Files.createDirectories(cachedFile.toPath().getParent());
                Files.write(cachedFile.toPath(), is.readAllBytes());
            } else {
                log.info("Loading WMTS capabilities from cached file {}", cachedFile);
            }
            return parseCapabilitiesFile(cachedFile);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Capablities.xml", e);
        }

    }

    public MapDefinition loadMapDefinitionFile(File mapDefinitionFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(MapDefinition.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (MapDefinition) jaxb.unmarshal(mapDefinitionFile);
    }

    private Capabilities parseCapabilitiesFile(File capabilitiesFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Capabilities.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (Capabilities) jaxb.unmarshal(capabilitiesFile);
    }
}
