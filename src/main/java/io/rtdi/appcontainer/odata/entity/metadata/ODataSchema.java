package io.rtdi.appcontainer.odata.entity.metadata;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public class ODataSchema extends ODataBase {
	
	private EntityType entitytype;
	private EntityContainer container;
	private ODataIdentifier identifier;
	
	public ODataSchema() {};

	public ODataSchema(ODataIdentifier identifer, String tabletype) {
		container = new EntityContainer(identifer, tabletype);
		this.identifier = identifer;
		entitytype = new EntityType(identifer);
	}

	@XmlElement(name = "EntityType")
	@JsonIgnore
	public EntityType getEntityType() {
		return entitytype;
	}
	
	@XmlElement(name = "EntityContainer")
	@JsonIgnore
	public EntityContainer getEntityContainer() {
		return container;
	}
	
	@XmlAttribute(name="Namespace")
	@JsonIgnore
	public String getNamespace() {
		return identifier.getEntityName();
	}

	@JsonAnyGetter
	public Map<String, Object> getDataJson() {
		Map<String, Object> data = new HashMap<>();
		data.put(entitytype.getName(), entitytype);
		data.put(container.getName(), container);
		return data;
	}
	
}