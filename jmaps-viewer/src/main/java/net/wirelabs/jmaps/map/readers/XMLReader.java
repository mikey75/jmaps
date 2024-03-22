package net.wirelabs.jmaps.map.readers;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public abstract class XMLReader<C> {

    private File file;
    private Unmarshaller jaxb;
    private JAXBContext context;

    protected XMLReader(Class<C> theClass) throws JAXBException{


            context = JAXBContext.newInstance(theClass);
            jaxb = context.createUnmarshaller();

    }

    public C loadFromXmlFile(File file) throws JAXBException{
        return (C) jaxb.unmarshal(file);
    }


}
