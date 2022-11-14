package io.rtdi.appcontainer.odata.entity.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public class Annotation {
	private String term;
	private String value;

	public Annotation() {};
	
	public Annotation(String term, String value) {
		this.term = term;
		this.value = value;
	}
	
	@XmlAttribute(name = "Term")
	@JsonIgnore
	public String getTerm() {
		return term;
	}
	
	@XmlElement(name = "String")
	public String getValue() {
		return value;
	}
}
