package net.wirelabs.jmaps.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class TileMatrixSetLimits {
		@XmlElement(namespace = Namespace.WMTS, name = "TileMatrixLimits")
		private TileMatrixLimits[] tileMatrixLimits;

		public TileMatrixLimits getTileMatrixLimits(int idx) {
			return tileMatrixLimits[idx];
		}
	}