package io.rtdi.appcontainer.odata;

import jakarta.xml.bind.annotation.XmlElement;

public class MapElements {
	@XmlElement
	public String key;
	@XmlElement
	public Object value;

	public MapElements() {
	}

	public MapElements(String key, Object value) {
		this.key = key;
		this.value = value;
	}

}
