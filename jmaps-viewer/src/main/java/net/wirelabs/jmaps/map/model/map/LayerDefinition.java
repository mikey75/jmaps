package net.wirelabs.jmaps.map.model.map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.wirelabs.jmaps.map.layer.LayerType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
 * LayerDefinition descriptor for all possible layer types
 */
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "Layer")
public class LayerDefinition {

    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private String url;
    @XmlElement(required = true)
    private LayerType type;
    @XmlElement
    private String crs = "EPSG:3857"; // default crs/ settable
    @XmlElement
    boolean swapAxis = false;
    @XmlElement
    private int tileSize = 256;
    @XmlElement
    private int maxZoom = 18;
    @XmlElement
    private int minZoom = 0;
    @XmlElement
    private float opacity = 1.0f;
    @XmlElement
    private int zoomOffset = 0;

    // wmts maps may specify these
    @XmlElement
    private String tileMatrixSet;
    @XmlElement
    private String wmtsLayer;

    public LayerDefinition(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
