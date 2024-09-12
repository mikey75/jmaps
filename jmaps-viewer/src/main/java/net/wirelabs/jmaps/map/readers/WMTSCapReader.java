package net.wirelabs.jmaps.map.readers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.opengis.wmts.x10.CapabilitiesDocument;
import net.wirelabs.jmaps.map.exceptions.CriticalMapException;
import org.apache.xmlbeans.XmlException;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static net.wirelabs.jmaps.map.Defaults.DEFAULT_WMTS_DESCRIPTOR_CACHE;

/**
 * WMTS Capabilities descriptor reader
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WMTSCapReader {

    private static String descriptorCacheDir = DEFAULT_WMTS_DESCRIPTOR_CACHE;

    public static CapabilitiesDocument.Capabilities loadCapabilities(String getCapabilitiesUrl) {

        try {
            URI uri = URI.create(getCapabilitiesUrl);
            File cachedFile = Paths.get(descriptorCacheDir, uri.getHost(), uri.getPath(), "capabilities.xml").toFile();

            if (!cachedFile.exists()) {
                log.info("Loading WMTS capabilities from {}", getCapabilitiesUrl);
                return parseCapabilitiesFromNetwork(getCapabilitiesUrl, cachedFile);
            } else {
                log.info("Loading WMTS capabilities from cached file {}", cachedFile);
                return parseCapabilitiesFromFile(cachedFile);
            }
        } catch (Exception e) {
            String message = "Could not parse WMTS capabilities from " + getCapabilitiesUrl;
            log.warn(message);
            throw new CriticalMapException(message);
        }

    }

    private static CapabilitiesDocument.Capabilities parseCapabilitiesFromNetwork(String url, File cachedFile) throws IOException, XmlException {

        URL request = new URL(url);
        InputStream networkInputStream = request.openConnection().getInputStream();

        File tempFile = createTempFile(networkInputStream);
        CapabilitiesDocument.Capabilities caps = parseCapabilitiesFromFile(tempFile);

        cacheFile(cachedFile, tempFile);

        return caps;
    }

    private static void cacheFile(File cachedFile, File tempFile) throws IOException {
        Files.createDirectories(cachedFile.toPath().getParent());
        Files.move(tempFile.toPath(), cachedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }


    private static File createTempFile(InputStream is) throws IOException {
        File tempFile = File.createTempFile( "capabilities", ".xml" );
        Files.copy(is,tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return tempFile;
    }

    private static CapabilitiesDocument.Capabilities parseCapabilitiesFromFile(File capabilitiesFile) throws XmlException, IOException {
        CapabilitiesDocument c = CapabilitiesDocument.Factory.parse(capabilitiesFile);
        return c.getCapabilities();
    }

    public static void setCacheDir(String cacheDir) {
        descriptorCacheDir = cacheDir;
    }
}
