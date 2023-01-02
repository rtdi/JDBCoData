package io.rtdi.appcontainer.odata;

import java.util.ArrayList;
import java.util.List;

import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.definitions.PropertyRef;

public class ODataKeyClause extends ODataSQLStatementPart {

	public ODataKeyClause(String keys, EntityType table) throws ODataException {
		if (table == null) {
			throw new ODataException("No table metadata provided to the API call");			
		} else if (table.getPK() == null || table.getPK().size() == 0) {
			throw new ODataException("The table has no PK defined, hence does not support key access oData API calls");
		}
		List<PropertyRef> pk = table.getPK();
		String[] keyparts = keys.split(",");
		paramValues = new ArrayList<>();
		sql = new StringBuilder();
		if (keyparts.length == 1) {
			if (pk.size() == 1) {
				String odatapk = pk.get(0).getName();
				sql.append('"').append(ODataUtils.decodeName(odatapk)).append('"')
					.append(" = ?");
				paramValues.add(ODataTypes.convertToJDBC(keyparts[0], table.getPropertyMetadata(odatapk)));
			} else {
				throw new ODataException(String.format("The query got a single key \"%s\" but the table has %d keys",
						keys, pk.size()));
			}
		} else if (keyparts.length != pk.size()) {
			throw new ODataException(String.format("The query got a %d keys but the table has %d keys",
					keyparts.length, pk.size()));
		} else {
			for (String kvstring : keyparts) {
				String[] kv = kvstring.split("=");
				if (kv.length != 2) {
					throw new ODataException(String.format("The condition is \"%s\" but must be in the form of \"field=value\"",
							kvstring));							
				} else {
					String k = kv[0].trim();
					if (pk.contains(new PropertyRef(k))) {
						if (sql.length() != 0) {
							sql.append(" and ");
						}
						sql.append('"').append(ODataUtils.decodeName(k)).append('"')
							.append(" = ?");
						paramValues.add(ODataTypes.convertToJDBC(kv[1].trim(), table.getPropertyMetadata(k)));
					} else {
						throw new ODataException(String.format("The condition column is \"%s\" but such column does not exist as key ",
								kvstring));							
					}
				}
			}
		}
	}

}
