package io.rtdi.appcontainer.odata.entity.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlAttribute;

public class EntityContainerReference {
	private String entitytype;
	private String name;

	public EntityContainerReference(ODataIdentifier identifer, String tabletype) {
		this.entitytype = identifer.getEntityName() + "." + identifer.getEntityType();
		this.name = identifer.getEntitySetName();
	}

	@XmlAttribute(name = "Name")
	@JsonIgnore
	public String getName() {
		return name;
	}
	
	@XmlAttribute(name = "EntityType")
	@JsonProperty(ODataUtils.TYPE)
	public String getEntityType() {
		return entitytype;
	}
	
	@JsonProperty(ODataUtils.KIND)
	public String getKind() {
		return ODataKind.EntitySet.name();
	}

}