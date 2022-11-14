package io.rtdi.appcontainer.odata.entity.data;

import java.util.HashMap;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlElement;

public class ODataProperties {

	private Map<String, ODataProperty> properties;

	public ODataProperties() {
	}

	public ODataProperties(ODataRecord oDataRecord) {
		this.properties = new HashMap<>();
		for (String key : oDataRecord.keySet()) {
			properties.put(key, new ODataProperty(key, oDataRecord.get(key)));
		}
	}

	@XmlElement(name = "property", namespace = "http://docs.oasis-open.org/odata/ns/data")
	public Map<String, ODataProperty> getProperties() {
		return properties;
	}
}
