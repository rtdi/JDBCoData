package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

/**
 * Reading data from a JDBC ResultSet can be paused and continued in the next OData call.
 * This class holds all data needed for caching the states.
 * <br>
 * In particular it must support re-reading the same data again, if e.g. a http request had been interrupted and hence
 * the same data is requested a second time.
 * <br>
 * The method for enabling this is the $skiptoken inside an @odata.nextLink annotation.
 * The first query execution creates this object and in the result the @odata.nextLink is set to
 * the same URL (without the query parameters) plus a of $skiptoken=xxxxxxx000001. The first part of the number is
 * the resultsetid which uniquely identifies this object in the cache. The second half of the number is the page number requested.
 * So the nextLink tells to query this instance again but this time return page 1.
 *
 */
public class AsyncResultSetQuery extends AsyncResultSet {

	private String[] columnnames;
	private EntityType table;
	private Thread runner;
	private ReaderThread reader;

	/**
	 * Remember all states, create the SQL and execute it.
	 * 
	 * Note that this async process will close the connection. The caller must not close the connection until the entire data 
	 * had been fetched or a timeout event occurs. Also the connection must remain open for the entire time to avoid SQL errors.
	 * 
	 * @param connection A JDBC connection with its own lifetime
	 * @param identifier The schema/objectname
	 * @param select The raw OData $select string
	 * @param filter The raw OData $filter string
	 * @param order The raw OData $order string
	 * @param resultsetid An indicator for this this statement
	 * @param limit the maxpagesize to this many records
	 * @param resultsetrowlimit The absolute upper bound of row the query returns
	 * @param service The current service this is being called from
	 * @throws SQLException In case the provided parameters are wrong and the database complains
	 * @throws ODataException In case the provided parameters are logically incorrect
	 */
	public AsyncResultSetQuery(Connection connection, 
			ODataIdentifier identifier, String select, String filter, String order,
			String resultsetid, int limit, int resultsetrowlimit,
			JDBCoDataBase service) throws SQLException, ODataException {
		super(connection, identifier, resultsetid, limit, resultsetrowlimit, service);
		this.table = service.getMetadata(conn, identifier);
		ODataFilterClause where = new ODataFilterClause(filter, table);
		ODataSelectClause projection = new ODataSelectClause(select, table);
		ODataOrderByClause orderby = new ODataOrderByClause(order, table);
		// read all data ignoring skip/top
		sql = JDBCoDataService.createSQL(identifier, projection.getSQL(), where.getSQL(), orderby.getSQL(), null, resultsetrowlimit, table);
		reader = new ReaderThread(sql);
		reader.addParams(where.getParamValues());
		runner = new Thread(reader, "AsyncSQLReader_" + this.hashCode());
		runner.start();
	}

	/**
	 * @return the exception the reader thread faced or null
	 */
	@Override
	public Exception getError() {
		if (reader != null) {
			return reader.getError();
		} else {
			return null;
		}
	}
	
	private class ReaderThread implements Runnable {
		
		private String sql;
		private Exception error;
		private List<Object> paramValues;

		private ReaderThread(String sql) {
			this.sql = sql;
		}

		public void addParams(List<Object> params) {
			if (this.paramValues == null) {
				this.paramValues = params;
			} else {
				this.paramValues.addAll(params);
			}
		}

		@Override
		public void run() {
			try {
				try (PreparedStatement stmt = conn.prepareStatement(sql);) {
					if (paramValues != null) {
						for (int i = 0; i < paramValues.size(); i++) {
							stmt.setObject(i+1, paramValues.get(i));
						}
					}
					try (ResultSet rs = stmt.executeQuery();) {
						columnnames = new String[rs.getMetaData().getColumnCount()];
						for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
							columnnames[i] = ODataUtils.encodeName(rs.getMetaData().getColumnName(i+1));
						}
						while (rs.next()) {
							if (Thread.interrupted()) {
								throw new InterruptedException("SQL Reader thread was asked to interrupt");
							}
							ODataRecord row = new ODataRecord();
							for (int i=0; i<columnnames.length; i++) {
								row.put(columnnames[i], ODataTypes.convert(rs.getObject(i+1)));
							}
							rows.add(row);
						}
						readcompleted = true;
					}
				}
			} catch (Exception e) {
				this.error = e;
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					// ignore
				}
			}
		}
		
		public Exception getError() {
			return error;
		}
	}

	@Override
	protected boolean isAlive() {
		return runner.isAlive();
	}
}


