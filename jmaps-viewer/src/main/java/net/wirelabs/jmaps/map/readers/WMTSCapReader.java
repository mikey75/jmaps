package net.wirelabs.jmaps.map.readers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * WMTS Capabilities descriptor reader
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WMTSCapReader {

    private static String descriptorCacheDir = DEFAULT_WMTS_DESCRIPTOR_CACHE;

    public static Capabilities loadCapabilities(String getCapabilitiesUrl) {

        try {

            URI uri = URI.create(getCapabilitiesUrl);
            File cachedFile = Paths.get(descriptorCacheDir, uri.getHost(), uri.getPath(), "capabilities.xml").toFile();

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

    private static Capabilities parseCapabilitiesFile(File capabilitiesFile) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Capabilities.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (Capabilities) jaxb.unmarshal(capabilitiesFile);
    }

    static void setCacheDir(String cacheDir) {
        descriptorCacheDir = cacheDir;
    }
}
