package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import javax.naming.NamingException;

public abstract class JDBCoDataBase {

	public static final String RESULTSETCACHE = "RESULTSETCACHE";
	public static final String TABLEMETADATACACHE = "TABLEMETADATACACHE";

	@Context
	protected Configuration configuration;
	@Context
	protected ServletContext servletContext;
	@Context
	protected HttpServletRequest request;

	protected static Response createResponse(int httpstatus, Object entity, String format, HttpServletRequest request) {
		ResponseBuilder r = Response.status(httpstatus).entity(entity).header("OData-Version", ODataUtils.VERSIONVALUE);
		if (format != null) {
			/*
			 * If a valid format parameter has been passed, it takes precedence.
			 */
			if (format.equals("json")) {
				r = r.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
			} else if (format.equals("xml")) {
				r = r.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
			}
		} else if (request.getHeader(HttpHeaders.ACCEPT) == null) {
			/*
			 * Default format is XML if neither a $format nor an accept header has been sent
			 */
			r = r.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
		}
		return r.build();
	}

	public static AsyncResultSet getResultSetCache(HttpServletRequest request, String resultsetid) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<String, AsyncResultSet> cache = (Cache<String, AsyncResultSet>) session.getAttribute(RESULTSETCACHE);
			if (cache == null) {
				return null;
			} else {
				return cache.getIfPresent(resultsetid);
			}
		} else {
			return null;
		}
	}

	protected ODataSchema getMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException {
		HttpSession session = request.getSession(false);
		ODataSchema table;
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<ODataIdentifier, ODataSchema> cache = (Cache<ODataIdentifier, ODataSchema>) session.getAttribute(TABLEMETADATACACHE);
			if (cache == null) {
				cache = Caffeine.newBuilder()
						.expireAfterWrite(Duration.ofMinutes(getTableMetadataCacheTimeout()))
						.build();
				session.setAttribute(TABLEMETADATACACHE, cache);
			}
			table = cache.getIfPresent(identifier);
			if (table == null) {
				table = readTableMetadata(conn, identifier);
				cache.put(identifier, table);
			}
		} else {
			table = readTableMetadata(conn, identifier);
		}
		return table;
	}

	protected abstract ODataSchema readTableMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException;

	/**
	 * A table might get altered - frequently in the development system, less frequently in the production system. 
	 * By overwriting this return value, this can be controlled from the outside.
	 * 
	 * @return the number of minutes the table metadata should be caches since last read - default is 5 minutes
	 */
	protected long getTableMetadataCacheTimeout() {
		return 5L;
	}

	public JDBCoDataBase() {
		super();
	}

	public boolean addToResultSetCache(String resultsetid, AsyncResultSet resultset) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<String, AsyncResultSet> cache = (Cache<String, AsyncResultSet>) session.getAttribute(RESULTSETCACHE);
			if (cache == null) {
				cache = Caffeine.newBuilder()
						.expireAfterAccess(Duration.ofMinutes(getResultSetCacheTimeout()))
						.build();
				session.setAttribute(RESULTSETCACHE, cache);
			}
			if (cache.getIfPresent(resultsetid) == null) {
				cache.put(resultsetid, resultset);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return the number of minutes the resultset should be retained in the cache since last access - default is 15 minutes
	 */
	protected long getResultSetCacheTimeout() {
		return 15L;
	}

	/**
	 * This method should return a JDBC connection. It shall not be closed by the caller as its life cycle is completely controlled by
	 * the reader thread.
	 * 
	 * @return a new JDBC Connection
	 * @throws SQLException JDBC errors
	 * @throws ServletException other errors
	 * @throws NamingException JNDI lookup errors
	 */
	protected abstract Connection getConnection() throws SQLException, ServletException, NamingException;

 	protected ODataIdentifier createODataIdentifier(String schema, String objectname) {
		return new ODataIdentifier(schema, objectname);
	}

	public String getURL() {
		return request.getRequestURI();
	}
}