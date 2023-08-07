package net.wirelabs.jmaps.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public  class TileMatrixSetLink {
	@XmlElement(namespace = Namespace.WMTS, name = "TileMatrixSet")
	private String tileMatrixSet;
	@XmlElement(namespace = Namespace.WMTS, name = "TileMatrixSetLimits")
	private TileMatrixSetLimits tileMatrixSetLimits;
}
