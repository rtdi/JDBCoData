package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

/**
 * The ODataSchema is a data model and it consists of table structures (=EntityTypes) and
 * a single EntityContainer which contains all tables of a given structure.
 *
 */
public class ODataSchema extends ODataBase {
	
	private List<EntityType> entitytypes;
	private EntityContainer container;
	private String dbschema;
	
	public ODataSchema(String dbschema) {
		container = new EntityContainer(ODataUtils.CONTAINER);
		this.dbschema = dbschema;
		entitytypes = new ArrayList<>();
	}

	@XmlElement(name = "EntityType")
	@JsonIgnore
	public List<EntityType> getEntityTypes() {
		return entitytypes;
	}
	
	@XmlElement(name = "EntityContainer")
	@JsonIgnore
	public EntityContainer getEntityContainer() {
		return container;
	}
	
	@XmlAttribute(name="Namespace")
	@JsonIgnore
	public String getNamespace() {
		return dbschema;
	}

	@JsonAnyGetter
	public Map<String, Object> getDataJson() {
		Map<String, Object> data = new HashMap<>();
		data.put(container.getName(), container);
		for (EntityType type : entitytypes) {
			data.put(type.getName(), type);
		}
		return data;
	}

	@Override
	public String toString() {
		return String.format("OData schema %s with container %s and EntityTypes %s", dbschema, container.getName(), entitytypes);
	}

	public void addEntityType(EntityType entitytype) {
		entitytypes.add(entitytype);
		container.addTable(entitytype);
	}

}