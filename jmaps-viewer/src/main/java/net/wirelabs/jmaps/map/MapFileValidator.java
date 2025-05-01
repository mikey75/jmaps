package net.wirelabs.jmaps.map;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Objects;


@Slf4j

public class MapFileValidator {

    private Validator validator;

    public MapFileValidator() {

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            String fileUrl = Objects.requireNonNull(getClass().getClassLoader().getResource("schemas/mapDefinition.xsd")).getFile();
            Source schemaFile = new StreamSource(fileUrl);
            validator = factory.newSchema(schemaFile).newValidator();
        } catch (SAXException | NullPointerException e) {
            log.error("Map validator failed to start, cause: {}", e.getMessage());
        }
    }

    public boolean validateMapFile(File mapDefinitionFile) {
        try {
            validator.validate(new StreamSource(mapDefinitionFile));
            return true;
        } catch (SAXException e) {
            log.error("XML of map file {} does not conform with map file schema", mapDefinitionFile.getName());
            log.error("The cause of non-conformity is: {}", e.getMessage());
            return false;
        } catch (IOException e) {
            log.error("IO Exception while parsing XML: {}", e.getMessage());
            return false;
        }
    }

}
