package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.SQLException;

public class AsyncResultSetStatic extends AsyncResultSet {

	public AsyncResultSetStatic(Connection connection, ODataIdentifier identifier, String resultsetid, int limit,
			int resultsetrowlimit, JDBCoDataBase service) throws SQLException, ODataException {
		super(connection, identifier, resultsetid, limit, resultsetrowlimit, service);
	}

	@Override
	protected boolean isAlive() {
		return false;
	}

	@Override
	public Exception getError() {
		return null;
	}

}
