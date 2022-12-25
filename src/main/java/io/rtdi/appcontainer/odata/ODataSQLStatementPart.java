package io.rtdi.appcontainer.odata;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ODataSQLStatementPart {
	protected StringBuilder sql;
	protected List<Object> paramValues;
	
	public List<Object> getParamValues() {
		return paramValues;
	}

	public StringBuilder getSQL() {
		return sql;
	}

	public void setPreparedStatementParameters(PreparedStatement stmt) throws SQLException {
		for (int i=0; i<paramValues.size(); i++) {
			stmt.setObject(i+1, paramValues.get(i));
		}
	}

}
