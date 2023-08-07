package net.wirelabs.jmaps.utils;


import lombok.Getter;
import net.wirelabs.jmaps.map.layer.wmts.Capabilities;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created 5/14/23 by Michał Szwaczko (mikey@wirelabs.net)
 * <p>
 * Reads WMTS service descriptor
 */
@Getter

public class CapabilitiesReader {


    private  final String wmtsCacheDir;

    public CapabilitiesReader(String wmtsCacheDir) {
        this.wmtsCacheDir = wmtsCacheDir;
    }

    public CapabilitiesReader() {
        this.wmtsCacheDir = Paths.get(System.getProperty("user.home"), ".jmaps-cache","wmts-cache").toString();
    }

    public Capabilities getCapabilities(String getCapabilitiesUrl) {

        URI uri = URI.create(getCapabilitiesUrl);
        File cachedFile = Paths.get(wmtsCacheDir,uri.getHost(), uri.getPath(), "capabilities.xml").toFile();

        try {
            if (!cachedFile.exists()) {
                URL request = new URL(getCapabilitiesUrl);
                InputStream is = request.openConnection().getInputStream();
                Files.createDirectories(cachedFile.toPath().getParent());
                Files.write(cachedFile.toPath(), is.readAllBytes());
            }
            return parse(cachedFile);
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse Capablities.xml", e);
        }

    }

    private Capabilities parse(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Capabilities.class);
        Unmarshaller jaxb = context.createUnmarshaller();
        return (Capabilities) jaxb.unmarshal(file);
    }


}

