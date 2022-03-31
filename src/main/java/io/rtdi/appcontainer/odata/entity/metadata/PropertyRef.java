package io.rtdi.appcontainer.odata.entity.metadata;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import jakarta.xml.bind.annotation.XmlAttribute;

public class PropertyRef {

	private String name;
	
	public PropertyRef() {};

	public PropertyRef(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "Name")
	@JsonAnyGetter
	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object o) {
		return name.equals(o);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
