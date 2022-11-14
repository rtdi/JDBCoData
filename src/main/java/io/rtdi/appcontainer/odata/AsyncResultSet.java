package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.data.ODataResultSet;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

/**
 * This class holds all data needed for caching the states.
 * <br>
 * It also supports $skiptoken inside an @odata.nextLink annotation.
 * The first query execution creates this object and in the result the @odata.nextLink is set to
 * the same URL (without the query parameters) plus a of $skiptoken=xxxxxxx000001. The first part of the number is
 * the resultsetid which uniquely identifies this object in the cache. The second half of the number is the page number requested.
 * So the nextLink tells to query this instance again but this time return page 1.
 *
 */
public abstract class AsyncResultSet {

	protected Connection conn;
	protected String resultsetid;
	protected String url;
	protected List<ODataRecord> rows = Collections.synchronizedList(new ArrayList<>());
	protected ODataSchema table;
	protected final ODataIdentifier identifier;
	protected int limit;
	protected boolean readcompleted = false;

	/**
	 * Remember all states, create the SQL and execute it.
	 * 
	 * Note that this async process will close the connection. The caller must not close the connection until the entire data 
	 * had been fetched or a timeout event occurs. Also the connection must remain open for the entire time to avoid SQL errors.
	 * 
	 * @param connection A JDBC connection with its own lifetime
	 * @param identifier The schema/objectname
	 * @param resultsetid An indicator for this this statement
	 * @param limit the maxpagesize to this many records
	 * @param resultsetrowlimit The absolute upper bound of row the query returns
	 * @param service The current service this is being called from
	 * @throws SQLException In case the provided parameters are wrong and the database complains
	 * @throws ODataException In case the provided parameters are logically incorrect
	 */
	public AsyncResultSet(Connection connection, 
			ODataIdentifier identifier,
			String resultsetid, int limit, int resultsetrowlimit,
			JDBCoDataBase service) throws SQLException, ODataException {
		this.conn = connection;
		this.resultsetid = resultsetid;
		this.url = service.getURL();
		this.limit = limit;
		this.identifier = identifier;
		this.table = service.getMetadata(conn, identifier);
	}

	/**
	 * Provides the data in the ODataResultSet format for the requested segment
	 * This supports server side and client side paging.
	 * 
	 * @param skip Number of records to skip from the beginning
	 * @param top Max number of rows to return
	 * @param page the page number to return the data for, starting with 0
	 * @return the resultset object with up to limit-many rows
	 * @throws SQLException thrown when there are JDBC errors
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
			if (!isAlive()) {
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
		ODataResultSet resultset = new ODataResultSet(identifier);
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
	
	protected abstract boolean isAlive();

	/**
	 * @return true if the read was completed successfully
	 */
	public boolean readComplete() {
		return readcompleted;
	}

	public static String tokenToResultsetid(String token) {
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
	
	/**
	 * @return the exception the reader thread faced or null
	 */
	public abstract Exception getError();
	
}


