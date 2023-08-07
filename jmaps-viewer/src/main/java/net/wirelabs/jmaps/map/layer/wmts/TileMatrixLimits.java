package net.wirelabs.jmaps.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class TileMatrixLimits {
		@XmlElement(namespace = Namespace.WMTS, name = "TileMatrix")
		private String tileMatrix;
		@XmlElement(namespace = Namespace.WMTS, name = "MinTileRow")
		private int minTileRow;
		@XmlElement(namespace = Namespace.WMTS, name = "MaxTileRow")
		private int maxTileRow;
		@XmlElement(namespace = Namespace.WMTS, name = "MinTileCol")
		private int minTileCol;
		@XmlElement(namespace = Namespace.WMTS, name = "MaxTileCol")
		private int maxTileCol;
	}