package net.wirelabs.jmaps.viewer.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class TileMatrix {
		@XmlElement(namespace = Namespace.OWS, name = "Identifier")
		private String identifier;
		@XmlElement(namespace = Namespace.WMTS, name = "ScaleDenominator")
		private double scaleDenominator;
		@XmlElement(namespace = Namespace.WMTS, name = "TopLeftCorner")
		@XmlList
		private double[] topLeftCorner;
		@XmlElement(namespace = Namespace.WMTS, name ="TileWidth")
		private int tileWidth;
		@XmlElement(namespace = Namespace.WMTS, name = "TileHeight")
		private int tileHeight;
		@XmlElement(namespace = Namespace.WMTS, name = "MatrixWidth")
		private int matrixWidth;
		@XmlElement(namespace = Namespace.WMTS, name = "MatrixHeight")
		private int matrixHeight;
	}