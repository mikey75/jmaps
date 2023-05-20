package net.wirelabs.jmaps.viewer.map.layer.wmts;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Capabilities", namespace = Namespace.WMTS)
@Getter
public class Capabilities {

	@XmlElement(namespace = Namespace.WMTS, name = "Contents")
	private Contents contents;
}