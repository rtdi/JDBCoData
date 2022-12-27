package io.rtdi.appcontainer.odata.entity.metadata;

import java.util.LinkedHashMap;

import io.rtdi.appcontainer.odata.ODataKind;
import io.rtdi.appcontainer.odata.ODataUtils;

public class ODataEntityMetadata extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = -400162954157198175L;
	
	public ODataEntityMetadata(String name, String url) {
		this.put("name", name);
		this.put(ODataUtils.KIND, ODataKind.EntitySet.name());
		this.put("url", url);
	}
	
	@Override
	public String toString() {
		return String.format("EntityMetadata %s with URL %s", get("name"), get("url"));
	}

}