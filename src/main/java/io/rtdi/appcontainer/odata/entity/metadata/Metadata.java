package io.rtdi.appcontainer.odata.entity.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.rtdi.appcontainer.odata.ODataUtils;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.definitions.ODataSchema;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This is the root of the metadata document.
 * It consists of a single schema (=data model)
 *
 */
@XmlRootElement(name = "Edmx", namespace = "http://docs.oasis-open.org/odata/ns/edmx")
public class Metadata {
	private String version;
	private Map<String, ODataSchema> schemas;
	/*
	 	http://localhost:8080/AppContainer/protected/rest/odata/tables/INFORMATION_SCHEMA/DATABASES/$metadata

<edmx:Edmx
	xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
	<edmx:DataServices>
		<Schema
			xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="DBOBJECT">
			<EntityType Name="ROW">
				<Key>
					<PropertyRef Name="__ROWNUM"/>
				</Key>
				<Property Name="DATABASE_NAME" Type="Edm.String" MaxLength="16777216"/>
				<Property Name="DATABASE_OWNER" Type="Edm.String" MaxLength="16777216"/>
				<Property Name="IS_TRANSIENT" Type="Edm.String" MaxLength="3"/>
				<Property Name="COMMENT" Type="Edm.String" MaxLength="16777216"/>
				<Property Name="CREATED" Type="Edm.DateTimeOffset"/>
				<Property Name="LAST_ALTERED" Type="Edm.DateTimeOffset"/>
				<Property Name="RETENTION_TIME" Type="Edm.Int64"/>
				<Property Name="__ROWNUM" Type="Edm.Int32"/>
			</EntityType>
			<EntityContainer Name="CONTAINER">
				<EntitySet Name="TABLE" EntityType="DBOBJECT.ROW">
					<Annotation Term="jdbc.jdbc.schemaname">
						<String>INFORMATION_SCHEMA</String>
					</Annotation>
					<Annotation Term="jdbc.jdbc.objectname">
						<String>DATABASES</String>
					</Annotation>
				</EntitySet>
			</EntityContainer>
		</Schema>
	</edmx:DataServices>
</edmx:Edmx>

{
  "$Version": "4.01",
  "DBOBJECT": {
    "ROW": {
      "$Kind": "EntityType",
      "$Key": [
        "__ROWNUM"
      ],
      "DATABASE_NAME": {
        "$Type": "Edm.String",
        "$MaxLength": 16777216
      },
      "DATABASE_OWNER": {
        "$Type": "Edm.String",
        "$MaxLength": 16777216
      },
      "IS_TRANSIENT": {
        "$Type": "Edm.String",
        "$MaxLength": 3
      },
      "COMMENT": {
        "$Type": "Edm.String",
        "$MaxLength": 16777216
      },
      "CREATED": {
        "$Type": "Edm.DateTimeOffset"
      },
      "LAST_ALTERED": {
        "$Type": "Edm.DateTimeOffset"
      },
      "RETENTION_TIME": {
        "$Type": "Edm.Int64"
      },
      "__ROWNUM": {
        "$Type": "Edm.Int32"
      }
    },
    "CONTAINER": {
      "$Kind": "EntityContainer",
      "TABLE": {
        "$Kind": "EntitySet",
        "$Type": "DBOBJECT.ROW",
        "@jdbc.jdbc.schemaname": "INFORMATION_SCHEMA",
        "@jdbc.jdbc.objectname": "DATABASES"
      }
    }
  }
}
	 */
	
	public Metadata() {
		this.version = "4.0";
		this.schemas = new HashMap<>();
		schemas.put(ODataUtils.SCHEMA, new ODataSchema(ODataUtils.SCHEMA));
	}
	
	@XmlAttribute(name="Version")
	@JsonProperty(ODataUtils.VERSION)
	public String getVersion() {
		return version;
	}
	
	public void addObject(EntityType tableobject) {
		if (tableobject != null) {
			ODataSchema schema = schemas.get(ODataUtils.SCHEMA);
			schema.addEntityType(tableobject);
		}
	}
	
	@XmlElementWrapper(name="DataServices", namespace = "http://docs.oasis-open.org/odata/ns/edmx")
	@XmlElement(name="Schema", namespace = "http://docs.oasis-open.org/odata/ns/edm")
	@JsonIgnore
	public Collection<ODataSchema> getSchemas() {
		return schemas.values();
	}
	
	@JsonAnyGetter
	public Map<String, ODataSchema> getSchemasJson() {
		return schemas;
	}
	
	@Override
	public String toString() {
		return String.format("Metadata for %d schemas (=tables)", schemas.size());
	}

}