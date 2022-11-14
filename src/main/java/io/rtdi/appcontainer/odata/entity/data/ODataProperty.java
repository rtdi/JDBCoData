package io.rtdi.appcontainer.odata.entity.data;

import jakarta.xml.bind.annotation.XmlElement;

public class ODataProperty {
	private String name;
	private Object value;

	public ODataProperty(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	@XmlElement
	public Object getValue() {
		return value;
	}

}
