package net.wirelabs.jmaps.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class Contents {
		@XmlElement(namespace = Namespace.WMTS, name = "Layer")
		private Layer[] layers;
		@XmlElement(namespace = Namespace.WMTS, name = "TileMatrixSet")
		private TileMatrixSet[] tileMatrixSets;

		// get tms by index
		public TileMatrixSet getTileMatrixSet(int idx) {
			return tileMatrixSets[idx];
		}
		// get tms by name
		public TileMatrixSet getTileMatrixSet(String name) {
			for (TileMatrixSet tms: tileMatrixSets) {
				if (tms.getIdentifier().equals(name)) return tms;
			}
			return getTileMatrixSet(0); // todo, throw exception rather than setting defaults
		}

		public Layer getLayer(int idx) {
			return layers[idx];
		}
		public Layer getLayer(String id) {
			for (Layer l: layers) {
				if (l.getIdentifier().equals(id)) return l;
			}
			return layers[0];// todo, throw exception rather than setting defaults
		}
	}
