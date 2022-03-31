package io.rtdi.appcontainer.odata;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ODataSQLStatementPart {
	protected StringBuilder sql;
	protected List<Object> params;
	
	public List<Object> getParams() {
		return params;
	}

	public StringBuilder getSQL() {
		return sql;
	}

	public void setPreparedStatementParameters(PreparedStatement stmt) throws SQLException {
		for (int i=0; i<params.size(); i++) {
			stmt.setObject(i+1, params.get(i));
		}
	}

}
