package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public class EntityContainer {
	private Map<String, EntityContainerReference> tables;
	private String containername;
	
	public EntityContainer(String containername) {
		tables = new HashMap<>();
		this.containername = containername;
	}
	
	@XmlAttribute(name = "Name")
	@JsonIgnore
	public String getName() {
		return containername;
	}
	
	@XmlElement(name = "EntitySet")
	@JsonIgnore
	public Collection<EntityContainerReference> getEntitySet() {
		return tables.values();
	}
	
	@JsonProperty(ODataUtils.KIND)
	public String getKind() {
		return ODataKind.EntityContainer.name();
	}

	@JsonAnyGetter
	public Map<String, EntityContainerReference> getDataJson() {
		return tables;
	}
	
	public void addTable(EntityType table) {
		tables.put(table.getName(), new EntityContainerReference(table.getIdentifier()));
	}
	
	@Override
	public String toString() {
		return String.format("EntityContainer %s for table %s", containername, tables.values());
	}

}