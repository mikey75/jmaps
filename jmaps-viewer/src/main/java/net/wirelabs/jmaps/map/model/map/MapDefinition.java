package net.wirelabs.jmaps.map.model.map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@Setter
public class MapDefinition  {

    @XmlAttribute(name = "name")
    private String name;
    @XmlElement(name = "layer")
    private List<LayerDefinition> layers = new ArrayList<>();
    @XmlElement(name = "copyright")
    private String copyright;

    public MapDefinition(String name, String copyright) {
        this.name = name;
        this.copyright = copyright;
    }

    public void addLayer(LayerDefinition layer) {
        layers.add(layer);
    }
}

