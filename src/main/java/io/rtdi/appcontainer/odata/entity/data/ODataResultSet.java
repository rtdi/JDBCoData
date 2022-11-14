package io.rtdi.appcontainer.odata.entity.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "feed", namespace = "http://www.w3.org/2005/Atom")
public class ODataResultSet extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 6250500946106557033L;

	/*
	 	http://localhost:8080/AppContainer/protected/rest/odata/tables/INFORMATION_SCHEMA/DATABASES/TABLE

<a:feed
	xmlns:a="http://www.w3.org/2005/Atom"
	xmlns:m="http://docs.oasis-open.org/odata/ns/metadata"
	xmlns:d="http://docs.oasis-open.org/odata/ns/data" m:context="$metadata#TABLE">
	<a:id>http://localhost:8080/AppContainer/protected/odata/INFORMATION_SCHEMA/DATABASES/TABLE</a:id>
	<a:entry>
		<a:title/>
		<a:summary/>
		<a:updated>2022-03-27T09:55:00Z</a:updated>
		<a:author>
			<a:name/>
		</a:author>
		<a:category scheme="http://docs.oasis-open.org/odata/ns/scheme" term="#DBOBJECT.ROW"/>
		<a:content type="application/xml">
			<m:properties>
				<d:DATABASE_NAME>DEMO_DB</d:DATABASE_NAME>
				<d:DATABASE_OWNER>SYSADMIN</d:DATABASE_OWNER>
				<d:IS_TRANSIENT>NO</d:IS_TRANSIENT>
				<d:COMMENT>demo database</d:COMMENT>
				<d:CREATED m:type="DateTimeOffset">2020-09-15T16:30:02.161Z</d:CREATED>
				<d:LAST_ALTERED m:type="DateTimeOffset">2020-09-15T16:30:02.161Z</d:LAST_ALTERED>
				<d:RETENTION_TIME m:type="Int64">1</d:RETENTION_TIME>
			</m:properties>
		</a:content>
	</a:entry>
	<a:entry>...
</a:feed>

{
  "@odata.context": "$metadata#TABLE",
  "value": [
    {
      "DATABASE_NAME": "DEMO_DB",
      "DATABASE_OWNER": "SYSADMIN",
      "IS_TRANSIENT": "NO",
      "COMMENT": "demo database",
      "CREATED": "2020-09-15T16:30:02.161Z",
      "LAST_ALTERED": "2020-09-15T16:30:02.161Z",
      "RETENTION_TIME": 1,
      "__ROWNUM": 0
    },
    {...
  ]
}

	 */
	
	private List<ODataRecord> value = new ArrayList<>();
	
	public ODataResultSet(ODataIdentifier identifier) {
		this.put(ODataUtils.ODATACONTEXT, ODataUtils.METADATA + "#" + identifier.getEntitySetName());
		this.put(ODataUtils.VALUE, value);
	}
	
	public void addRow(ODataRecord row) {
		value.add(row);
	}
	
	@JsonIgnore
	@XmlElement(name = "entry", namespace = "http://www.w3.org/2005/Atom")
	public List<ODataRecord> getEntries() {
		return value;
	}
	
	public void setNextLink(String nextlink) {
		this.put(ODataUtils.NEXTLINK, nextlink);
	}
}
