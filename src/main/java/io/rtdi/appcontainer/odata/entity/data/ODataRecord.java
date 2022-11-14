package io.rtdi.appcontainer.odata.entity.data;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.xml.bind.annotation.XmlElement;

public class ODataRecord extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = -1963053479049674975L;

	@JsonIgnore
	@XmlElement(name = "content", namespace = "http://www.w3.org/2005/Atom")
	public ODataContent getContent() {
		return new ODataContent(this);
	}
	
	public static class ODataContent {

		private ODataRecord record;

		public ODataContent(ODataRecord record) {
			this.record = record;
		}

		@XmlElement(name = "properties", namespace = "http://docs.oasis-open.org/odata/ns/metadata")
		public ODataProperties getContent() {
			return new ODataProperties(record);
		}

	}
}
