package io.rtdi.appcontainer.odata.entity.metadata;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public class EntityContainer {
	private EntityContainerReference table;
	
	public EntityContainer(ODataIdentifier identifier, String tabletype) {
		table = new EntityContainerReference(identifier, tabletype);
	}
	
	@XmlAttribute(name = "Name")
	@JsonIgnore
	public String getName() {
		return ODataUtils.CONTAINER;
	}
	
	@XmlElement(name = "EntitySet")
	@JsonIgnore
	public EntityContainerReference getEntitySet() {
		return table;
	}
	
	@JsonProperty(ODataUtils.KIND)
	public String getKind() {
		return ODataKind.EntityContainer.name();
	}

	@JsonAnyGetter
	public Map<String, EntityContainerReference> getDataJson() {
		Map<String, EntityContainerReference> data = new HashMap<>();
		data.put(table.getName(), table);
		return data;
	}
	
}