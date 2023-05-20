package net.wirelabs.jmaps.viewer.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class WGS84BoundingBox {
		@XmlElement(namespace = Namespace.OWS, name = "LowerCorner")
		private String lowerCorner; // lower left
		@XmlElement(namespace = Namespace.OWS,name = "UpperCorner")
		private String upperCorner; // upper right
	}
