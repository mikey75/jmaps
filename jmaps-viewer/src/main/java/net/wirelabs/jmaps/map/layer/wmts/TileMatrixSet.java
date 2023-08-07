package net.wirelabs.jmaps.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class TileMatrixSet {
		@XmlElement(namespace = Namespace.OWS, name = "Identifier")
		private String identifier;
		@XmlElement(namespace = Namespace.OWS, name = "SupportedCRS")
		private String supportedCRS;
		@XmlElement(namespace = Namespace.WMTS, name = "TileMatrix")
		private TileMatrix[] tileMatrices;

		public TileMatrix getTileMatrix(int idx) {
			return tileMatrices[idx];
		}
	}