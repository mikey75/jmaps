package net.wirelabs.jmaps.viewer.map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.wirelabs.jmaps.viewer.map.layer.LayerType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created 6/5/23 by Michał Szwaczko (mikey@wirelabs.net)
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
    @XmlElement(required = false)
    boolean swapAxis = false;
    @XmlElement(required = false)
    private int tileSize = 256;
    @XmlElement(required = false)
    private int maxZoom = 18;
    @XmlElement(required = false)
    private int minZoom = 0;
    @XmlElement(required = false)
    private float opacity = 1.0f;
    @XmlElement(required = false)
    private int zoomOffset = 0;


    public LayerDefinition(String type, String name, String url) {
        this.name = name;
        this.url = url;
        this.type = LayerType.valueOf(type);
    }
    
    public LayerDefinition withSwapAxis(boolean val) {
        this.swapAxis = val;
        return this;
    }
    public LayerDefinition withMaxZoom( int zoom) {
        this.maxZoom = zoom;
        return this;
    }
    public LayerDefinition withMinZoom(int zoom) {
        this.minZoom = zoom;
        return this;
    }
    public LayerDefinition withTileSize(int tileSize) {
        this.tileSize = tileSize;
        return this;
    }
    
    public LayerDefinition withOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }
    public LayerDefinition withZoomOffset(int zoomOffset){
        this.zoomOffset = zoomOffset;
        return this;
    }
}
