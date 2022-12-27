package io.rtdi.appcontainer.odata.entity.metadata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.ODataUtils;

public class EntitySets extends LinkedHashMap<String, Object> {
	private static final long serialVersionUID = -7890139590145930819L;
	private List<ODataEntityMetadata> value = new ArrayList<>();
	
	public EntitySets() {
		this.put(ODataUtils.ODATACONTEXT, ODataUtils.METADATA);
		this.put(ODataUtils.VALUE, value);
	}
	
	public void addTable(String name) throws ODataException {
		value.add(new ODataEntityMetadata(name, name));
	}

	@Override
	public String toString() {
		return String.format("EntitySets with %d entries", value.size());
	}

}