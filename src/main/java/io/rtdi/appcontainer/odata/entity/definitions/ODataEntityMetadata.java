package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlElement;

public class ODataEntityMetadata extends LinkedHashMap<String, String> {
	private static final long serialVersionUID = -400162954157198175L;
	
	public ODataEntityMetadata(String name, String url) {
		this.put("name", name);
		this.put(ODataUtils.KIND, ODataKind.EntitySet.name());
		this.put("url", url);
	}
	
	@Override
	public String toString() {
		return String.format("EntityMetadata %s with URL %s", get("name"), get("url"));
	}

	@XmlElement(name="name")
	@JsonIgnore
	public String getName() {
		return this.get("name");
	}

	@XmlElement(name="kind")
	@JsonIgnore
	public String getKind() {
		return this.get(ODataUtils.KIND);
	}

	@XmlElement(name="url")
	@JsonIgnore
	public String getUrl() {
		return this.get("url");
	}

}