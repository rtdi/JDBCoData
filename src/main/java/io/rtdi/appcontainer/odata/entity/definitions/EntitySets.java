package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Edmx", namespace = "http://docs.oasis-open.org/odata/ns/edmx")
public class EntitySets extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = -7890139590145930819L;
	private List<ODataEntityMetadata> value = new ArrayList<>();
	
	public EntitySets() {
		this.put(ODataUtils.ODATACONTEXT, ODataUtils.METADATA);
		this.put(ODataUtils.VALUE, value);
	}
	
	public void addTable(String name) throws ODataException {
		value.add(new ODataEntityMetadata(name, name));
	}
	
	@XmlElementWrapper(name="DataServices", namespace = "http://docs.oasis-open.org/odata/ns/edmx")
	@XmlElement(name="Schemas")
	@JsonIgnore
	public List<ODataEntityMetadata> getSchemas() {
		return value;
	}


	@Override
	public String toString() {
		return String.format("EntitySets with %d entries", value.size());
	}

}