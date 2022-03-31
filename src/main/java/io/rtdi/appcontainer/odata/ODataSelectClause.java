package io.rtdi.appcontainer.odata;

import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public class ODataSelectClause extends ODataSQLStatementPart {

	public ODataSelectClause(String select, ODataSchema table) {
		if (select == null || select.trim().length() == 0) {
			this.sql = new StringBuilder("*");
		} else {
			String[] parts = select.split(",");
			this.sql = new StringBuilder();
			for (String part : parts) {
				String col = ODataUtils.decodeName(part.trim());
				if (sql.length() != 0) {
					sql.append(", ");
				}
				sql.append('"').append(col).append('"');
			}
		}
	}

}
