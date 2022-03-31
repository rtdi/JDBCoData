package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.data.ODataResultSet;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

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
public class AsyncResultSet {

	private Connection conn;
	private String[] columnnames;
	private String resultsetid;
	private String url;
	private List<ODataRecord> rows = Collections.synchronizedList(new ArrayList<>());
	private ODataSchema table;
	private Thread runner;
	private ReaderThread reader;
	private int limit;
	private boolean readcompleted = false;

	/**
	 * Remember all states, create the SQL and execute it.
	 * 
	 * Note that this async process will close the connection. The caller must not close the connection until the entire data 
	 * had been fetched or a timeout event occurs. Also the connection must remain open for the entire time to avoid SQL errors.
	 * 
	 * @param connection A JDBC connection with its own lifetime
	 * @param identifier
	 * @param select
	 * @param filter
	 * @param order
	 * @param skip
	 * @param top
	 * @param resultsetid
	 * @param service
	 * @throws SQLException
	 * @throws ODataException
	 */
	public AsyncResultSet(Connection connection, 
			ODataIdentifier identifier, String select, String filter, String order,
			String resultsetid, int limit,
			JDBCoDataService service) throws SQLException, ODataException {
		this.conn = connection;
		this.resultsetid = resultsetid;
		this.url = service.getURL();
		this.limit = limit;
		this.table = service.getMetadata(conn, identifier);
		ODataFilterClause where = new ODataFilterClause(filter, table);
		ODataSelectClause projection = new ODataSelectClause(select, table);
		// read all data ignoring skip/top
		String sql = JDBCoDataService.createSQL(identifier, projection.getSQL(), where.getSQL(), null, null, table);
		reader = new ReaderThread(sql);
		runner = new Thread(reader, "AsyncSQLReader_" + this.hashCode());
		runner.start();
	}

	/**
	 * Provides the data in the ODataResultSet format for the requested segment
	 * This supports server side and client side paging.
	 * 
	 * @param page the page number to return the data for, starting with 0
	 * @return the resultset object with up to limit-many rows
	 * @throws SQLException
	 */
	public ODataResultSet fetchRecords(Integer skip, Integer top, Integer page) throws SQLException {
		/*
		 * The user requested the data to start at <pre>skip</pre> or position 0 if not provided.
		 * But because the requested data was so much, server side pagination kicked in and a page is provided.
		 * In this case the start point for the resultset is page*limit
		 */
		int start = 0;
		if (skip != null) {
			start = skip;
		}
		if (page != null) {
			start += limit*page;
		}
		/*
		 * The user asked for top many rows. If this top value is more than the limit, the query returns just
		 * that many rows and adds a nextLink to enable reading the next page.
		 */
		int stop; // start=0; stop=100 means all rows with index=0...99
		if (top != null) {
			stop = start + Math.min(limit, top);
		} else {
			stop = start + limit;
		}
		long timeout = System.currentTimeMillis() + 60000L;
		/*
		 * Wait for the data to appear - up to 60 seconds.
		 */
		while (rows.size() <= stop && System.currentTimeMillis() < timeout) {
			/*
			 * If the database reader thread is finished, no more data will be added, and the read data can be returned.
			 */
			if (!runner.isAlive()) {
				break;
			}
			/*
			 * If the database reader is busy still, give it 0.5 seconds to produce the data and check again
			 */
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new SQLException("Fetch was interrupted");
			}
		}
		boolean hasmoredata = true;
		if (readcompleted) {
			/*
			 * All data has been read from the database, so reset the stop limit in 
			 */
			if (stop > rows.size()) {
				stop = rows.size();
				hasmoredata = false;
			}
		} else if (rows.size() < stop) {
			if (getError() != null) {
				throw new SQLException("Reader ran into an error", getError());
			}
			throw new SQLException("Fetch did not find the data in time");
		}
		// Yes, the data is available
		ODataResultSet resultset = new ODataResultSet();
		for (int i = start; i < stop; i++) {
			resultset.addRow(rows.get(i));
		}
		if (hasmoredata) {
			int newpage;
			if (page != null) {
				newpage = page+1;
			} else {
				newpage = 1;
			}
			String nextlinktoken = generateToken(resultsetid, newpage);
			resultset.setNextLink(url + "?$skiptoken=" + nextlinktoken);
		}
		return resultset;
	}
	
	public boolean readComplete() {
		return readcompleted;
	}

	public static String tokenToresultsetid(String token) {
		return token.substring(0, token.length()-6);
	}

	public static int tokenToPageid(String token) throws ODataException {
		try {
			return Integer.valueOf(token.substring(token.length()-6));
		} catch (NumberFormatException e) {
			throw new ODataException("Token is in the wrong format");
		}
	}

	public static String generateToken(String resultsetid, int page) {
		return resultsetid + String.format("%06d", page);
	}
	
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

		private ReaderThread(String sql) {
			this.sql = sql;
		}

		@Override
		public void run() {
			try {
				try (PreparedStatement stmt = conn.prepareStatement(sql);) {
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
}


