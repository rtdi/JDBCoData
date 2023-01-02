package io.rtdi.appcontainer.odata;

import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public class ODataOrderByClause extends ODataSQLStatementPart {

	public ODataOrderByClause(String keys, EntityType table) throws ODataException {
		if (keys != null) {
			sql = new StringBuilder();
			String[] parts = keys.split("[ ]*,[ ]*");
			for (String part : parts) {
				String fieldname;
				Boolean descending = null;
				int pos = part.indexOf(' ');
				if (pos == -1) {
					fieldname = part;
				} else {
					String orderterm = part.substring(pos).trim();
					if ("desc".equals(orderterm)) {
						descending = Boolean.TRUE;
					} else if ("asc".equals(orderterm)) {
						descending = Boolean.FALSE;
					}
					fieldname = part.substring(0, pos).trim();
				}
				if (sql.length() != 0) {
					sql.append(", ");
				}
				sql.append('"').append(ODataUtils.decodeName(fieldname)).append('"');
				if (descending != null && descending == Boolean.TRUE) {
					sql.append(" desc");
				}
			}
		}
	}

}

