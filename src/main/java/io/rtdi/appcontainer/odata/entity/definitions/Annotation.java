package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
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
	@JsonIgnore
	public String getValue() {
		return value;
	}
	
	/**
	 * @return a map of a key value pair so that the json looks like { term: value }
	 */
	@JsonAnyGetter
	public Map<String, String> getSchemasJson() {
		return Collections.singletonMap(term, value);
	}
	
	@Override
	public String toString() {
		return "Annotation " + term + " = " + value;
	}

}
