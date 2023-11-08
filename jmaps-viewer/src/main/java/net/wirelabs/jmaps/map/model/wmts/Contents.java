package net.wirelabs.jmaps.map.model.wmts;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;


@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
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
			throw new IllegalArgumentException("TileMatrixSet "+ name +" does not exist in the map");
		}

		public Layer getLayer(int idx) {
			return layers[idx];
		}

		public Layer getLayer(String id) {
			for (Layer layer: layers) {
				if (layer.getIdentifier().equals(id)) return layer;
			}
			throw new IllegalArgumentException("Layer " + id +" does not exist int the map");
		}
	}
