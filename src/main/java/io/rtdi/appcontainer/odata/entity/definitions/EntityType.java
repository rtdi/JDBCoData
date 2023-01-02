package io.rtdi.appcontainer.odata.entity.definitions;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

/**
 * An OData EntityType is the database table column list
 *
 */
public class EntityType extends ODataBase {
	private List<PropertyRef> key;
	private List<EntityTypeProperty> columns = new ArrayList<>();
	private Map<String, EntityTypeProperty> index = new HashMap<>();
	private String tabletype;
	private ODataIdentifier identifier;
	
	public EntityType() {};
	
	public EntityType(ODataIdentifier identifier) {
		this.identifier = identifier;
	}
	
	public EntityType(ODataIdentifier identifier, String tabletype) {
		this(identifier);
		this.tabletype = tabletype;
		addAnnotation(ODataUtils.JDBCTYPE, tabletype);
	}

	@XmlElementWrapper(name = "Key")
	@XmlElement(name="PropertyRef")
	@JsonProperty(ODataUtils.KEY)
	public List<PropertyRef> getPK() {
		return key;
	}

	public void addKey(String columnname) {
		if (key == null) {
			key = new ArrayList<>();
		}
		key.add(new PropertyRef(columnname));
	}
	
	public EntityTypeProperty addColumn(String columnname, JDBCType type, String typename, Integer length, Integer scale) {
		EntityTypeProperty col = new EntityTypeProperty(columnname, type, typename, length, scale);
		columns.add(col);
		index.put(columnname, col);
		col.addAnnotation(ODataUtils.JDBCDATATYPE, type.getName());
		return col;
	}
	
	public EntityTypeProperty getPropertyMetadata(String name) {
		return index.get(name);
	}
	
	@XmlElement(name="Property")
	@JsonIgnore
	public List<EntityTypeProperty> getProperties() {
		return columns;
	}
	
	@XmlAttribute(name = "Name")
	@JsonIgnore
	public String getName() {
		return identifier.getEntityType();
	}
	
	@JsonAnyGetter
	public Map<String, EntityTypeProperty> getSchemasJson() {
		return index;
	}

	@JsonProperty(ODataUtils.KIND)
	public String getKind() {
		return ODataKind.EntityType.name();
	}
	
	@Override
	public String toString() {
		return String.format("EntityType %s with %d properties", identifier.getEntityType(), index.size());
	}

	@JsonIgnore
	public String getTableType() {
		return tabletype;
	}

	public void setTableType(String tabletype) {
		this.tabletype = tabletype;
	}
	
	@JsonIgnore
	public ODataIdentifier getIdentifier() {
		return identifier;
	}

}