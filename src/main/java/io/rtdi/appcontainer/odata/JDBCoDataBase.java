package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.definitions.EntityTypeProperty;
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

public abstract class JDBCoDataBase {

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

	/**
	 * Used for the $count where only the number as such is returned
	 * 
	 * @param httpstatus
	 * @param entity
	 * @param request
	 * @return
	 */
	protected static Response createResponseText(int httpstatus, Object entity, HttpServletRequest request) {
		ResponseBuilder r = Response.status(httpstatus).entity(entity).header("OData-Version", ODataUtils.VERSIONVALUE);
		r = r.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
		return r.build();
	}

	public static AsyncResultSet getCachedResultSet(HttpServletRequest request, String resultsetid) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<String, AsyncResultSet> cache = (Cache<String, AsyncResultSet>) session.getAttribute("RESULTSETCACHE");
			if (cache == null) {
				return null;
			} else {
				return cache.getIfPresent(resultsetid);
			}
		} else {
			return null;
		}
	}

	protected EntityType getMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException {
		HttpSession session = request.getSession(false);
		EntityType table;
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<ODataIdentifier, EntityType> cache = (Cache<ODataIdentifier, EntityType>) session.getAttribute("TABLEMETADATACACHE");
			if (cache == null) {
				cache = Caffeine.newBuilder()
						.expireAfterWrite(Duration.ofMinutes(getTableMetadataCacheTimeout()))
						.build();
				session.setAttribute("TABLEMETADATACACHE", cache);
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

	protected EntityType readTableMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException {
		EntityType table = null;
		try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), identifier.getDBSchema(), identifier.getDBObjectName(), null); ) {
			if (rs.next()) {
				/*
					1.TABLE_CAT String => table catalog (may be null) 
					2.TABLE_SCHEM String => table schema (may be null) 
					3.TABLE_NAME String => table name 
					4.TABLE_TYPE String => table type. Typical types are "TABLE","VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY","LOCAL TEMPORARY", "ALIAS", "SYNONYM". 
					5.REMARKS String => explanatory comment on the table (may be null) 
					6.TYPE_CAT String => the types catalog (may be null) 
					7.TYPE_SCHEM String => the types schema (may be null) 
					8.TYPE_NAME String => type name (may be null) 
					9.SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null) 
					10.REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null) 
				 */
				String tabletype = rs.getString(4);
				String comment = rs.getString(5);
				switch (tabletype) {
				case "TABLE":
				case "VIEW":
				case "ALIAS":
				case "SYNONYM":
				case "SYSTEM TABLE":
					table = new EntityType(identifier, tabletype);
					table.setComment(comment);
					table.addAnnotation(ODataUtils.JDBCSCHEMANAME, identifier.getDBSchema());
					table.addAnnotation(ODataUtils.JDBCOBJECTNAME, identifier.getDBObjectName());
					break;
				default:
					throw new ODataException(String.format("The object \"%s\".\"%s\" is not a table/view/synonym", identifier.getDBSchema(), identifier.getDBObjectName()));
				}
			} else {
				throw new ODataException(String.format("The object \"%s\".\"%s\" was not found as table/view/synonym", identifier.getDBSchema(), identifier.getDBObjectName()));
			}
		}
		try (ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), identifier.getDBSchema(), identifier.getDBObjectName(), null); ) {
			while (rs.next()) {
				/*
					1.TABLE_CAT String => table catalog (may be null) 
					2.TABLE_SCHEM String => table schema (may be null) 
					3.TABLE_NAME String => table name 
					4.COLUMN_NAME String => column name 
					5.DATA_TYPE int => SQL type from java.sql.Types 
					6.TYPE_NAME String => Data source dependent type name,for a UDT the type name is fully qualified 
					7.COLUMN_SIZE int => column size. 
					8.BUFFER_LENGTH is not used. 
					9.DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable. 
					10.NUM_PREC_RADIX int => Radix (typically either 10 or 2) 
					11.NULLABLE int => is NULL allowed.
						- columnNoNulls - might not allow NULL values 
						- columnNullable - definitely allows NULL values 
						- columnNullableUnknown - nullability unknown 
					
					12.REMARKS String => comment describing column (may be null) 
					13.COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null) 
					14.SQL_DATA_TYPE int => unused 
					15.SQL_DATETIME_SUB int => unused 
					16.CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column 
					17.ORDINAL_POSITION int => index of column in table(starting at 1) 
					18.IS_NULLABLE String => ISO rules are used to determine the nullability for a column.
						- YES --- if the column can include NULLs 
						- NO --- if the column cannot include NULLs 
						- empty string --- if the nullability for the column is unknown 
					
					19.SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF) 
					20.SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF) 
					21.SCOPE_TABLE String => table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF) 
					22.SOURCE_DATA_TYPE short => source type of a distinct type or user-generatedRef type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF) 
					23.IS_AUTOINCREMENT String => Indicates whether this column is auto incremented
						- YES --- if the column is auto incremented 
						- NO --- if the column is not auto incremented 
						- empty string --- if it cannot be determined whether the column is auto incremented 
					24.IS_GENERATEDCOLUMN String => Indicates whether this is a generated column
						- YES --- if this a generated column 
						- NO --- if this not a generated column 
						- empty string --- if it cannot be determined whether this is a generated column 
				 */
				EntityTypeProperty col = table.addColumn(ODataUtils.encodeName(rs.getString(4)), JDBCType.valueOf(rs.getInt(5)), rs.getString(6), rs.getInt(7), rs.getInt(9));
				String nullable = rs.getString(18);
				if ("NO".equals(nullable)) {
					col.setNullable(Boolean.FALSE);
				} else if ("YES".equals(nullable)) {
					col.setNullable(Boolean.TRUE);
				}
				col.setComment(rs.getString(12));
			}			
		}
		try (ResultSet rs = conn.getMetaData().getPrimaryKeys(conn.getCatalog(), identifier.getDBSchema(), identifier.getDBObjectName()); ) {
			/*
				1.TABLE_CAT String => table catalog (may be null) 
				2.TABLE_SCHEM String => table schema (may be null) 
				3.TABLE_NAME String => table name 
				4.COLUMN_NAME String => column name 
				5.KEY_SEQ short => sequence number within primary key( a value of 1 represents the first column of the primary key, a value of 2 wouldrepresent the second column within the primary key). 
				6.PK_NAME String => primary key name (may be null) 
			 */
			while (rs.next()) {
				table.addKey(ODataUtils.encodeName(rs.getString(4)));
			}
		}
		return table;
	}

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
		HttpSession session = request.getSession(true);
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<String, AsyncResultSet> cache = (Cache<String, AsyncResultSet>) session.getAttribute("RESULTSETCACHE");
			if (cache == null) {
				cache = Caffeine.newBuilder()
						.expireAfterAccess(Duration.ofMinutes(getResultSetCacheTimeout()))
						.build();
				session.setAttribute("RESULTSETCACHE", cache);
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
	 */
	protected abstract Connection getConnection() throws SQLException, ServletException;

	protected ODataIdentifier createODataIdentifier(String schema, String objectname) {
		return new ODataIdentifier(schema, objectname);
	}

	protected ODataIdentifier createODataIdentifier(String schema, String objectname, String entityname) {
		return new ODataIdentifier(schema, objectname, entityname);
	}

	public String getURL() {
		return request.getRequestURI();
	}

	public static String createSQL(ODataIdentifier identifer, CharSequence projection, CharSequence where, CharSequence orderby, Integer skip, Integer top, EntityType table, Connection conn) throws SQLException {
		String driver = conn.getMetaData().getURL();
		boolean issqlserver = driver.startsWith("jdbc:sqlserver");
		if (top == null) {
			top = 5000;
		}
		StringBuilder sql = new StringBuilder("select ");
		sql.append(projection);
		sql.append(" from ").append(identifer.getIdentifier());
		if (where != null && where.length() != 0) {
			sql.append(" where ");
			sql.append(where);
		}
		if (orderby != null) {
			sql.append(" order by ").append(orderby);
		}
		if (issqlserver) {
			/*
			 * ORDER BY ...
			 * OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY;
			 */
			if (orderby == null) {
				sql.append(" order by 1 "); // offset/next requires an order by
			}
			if (skip == null) {
				skip = 0;
			}
			sql.append(" offset ").append(skip).append(" rows ");
			sql.append(" fetch next ").append(top).append(" rows only");
		} else {
			sql.append(" limit ").append(top);
			if (skip != null) {
				sql.append(" offset ").append(skip);
			}
		}
		return sql.toString();
	}

}