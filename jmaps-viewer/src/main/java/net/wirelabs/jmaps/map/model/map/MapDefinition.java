package net.wirelabs.jmaps.map.model.map;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Map definition xml
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "map")
@NoArgsConstructor
@Getter
public class MapDefinition  {

    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "layer")
    private List<LayerDefinition> layers = new ArrayList<>();
    @XmlElement(name = "copyright")
    private String copyright;
}

