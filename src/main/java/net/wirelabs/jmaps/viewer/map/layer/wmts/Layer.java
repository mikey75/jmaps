package net.wirelabs.jmaps.viewer.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class Layer {
		@XmlElement(namespace = Namespace.OWS, name = "Identifier")
		private String identifier;
		@XmlElement(namespace = Namespace.WMTS, name = "TileMatrixSetLink")
		private TileMatrixSetLink[] tileMatrixSetLinks;
		@XmlElement(namespace = Namespace.OWS, name = "WGS84BoundingBox")
		private WGS84BoundingBox wgs84BoundingBox;

		public TileMatrixSetLink getTileMatrixSetLink(int idx) {
			return tileMatrixSetLinks[idx];
		}
	}