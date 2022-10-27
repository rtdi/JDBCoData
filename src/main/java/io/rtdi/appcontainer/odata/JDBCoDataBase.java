package io.rtdi.appcontainer.odata;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.rtdi.appcontainer.odata.entity.metadata.EntityTypeProperty;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class JDBCoDataBase {

	@Context
	protected Configuration configuration;
	@Context
	protected ServletContext servletContext;
	@Context
	protected HttpServletRequest request;

	protected final static Cache<ODataIdentifier, ODataSchema> metadataCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(getTableMetadataCacheTimeout()))
			.build();

	public JDBCoDataBase() {
		super();
	}

	public AsyncResultSet getCachedResultSet(String resultSetId) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			@SuppressWarnings("unchecked")
			Cache<String, AsyncResultSet> cache = (Cache<String, AsyncResultSet>) session.getAttribute("RESULTSETCACHE");
			if (cache == null) {
				return null;
			} else {
				return cache.getIfPresent(resultSetId);
			}
		} else {
			return null;
		}
	}

	protected ODataSchema getMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException {
		ODataSchema table;
		table = metadataCache.getIfPresent(identifier);
		if (table == null) {
			table = readTableMetadata(conn, identifier);
			metadataCache.put(identifier, table);
		}
		return table;
	}

	protected ODataSchema readTableMetadata(Connection conn, ODataIdentifier identifier) throws SQLException, ODataException {
		ODataSchema table = null;
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
						table = new ODataSchema(identifier, tabletype);
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
				EntityTypeProperty col = table.getEntityType().addColumn(ODataUtils.encodeName(rs.getString(4)), JDBCType.valueOf(rs.getInt(5)), rs.getString(6), rs.getInt(7), rs.getInt(9));
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
				table.getEntityType().addKey(ODataUtils.encodeName(rs.getString(4)));
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
	protected static long getTableMetadataCacheTimeout() {
		return 15L;
	}

	public boolean addToResultSetCache(String resultsetid, AsyncResultSet resultset) {
		HttpSession session = request.getSession(false);
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
	 * @throws NamingException JNDI lookup errors
	 */
	protected abstract Connection getConnection() throws SQLException, ServletException, NamingException;

	protected ODataIdentifier createODataIdentifier(String schema, String objectname) {
		return new ODataIdentifier(schema, objectname);
	}

	public String getURL() {
		return request.getRequestURI();
	}

	/**
	 * To protect the server from caching millions of rows, there must be a hard limit of rows it produces at the outmost.
	 * The default is 5000 rows but it can be changed to any number larger than 100. It can even be dynamic based on the object
	 * data is read from or the typ of request - UI vs massdata consumers.
	 *
	 * @param request The httpRequest to decide on e.g. the type of query
	 * @param name is the database object name
	 * @param schema the database schema of the object
	 * @return the number of rows a query will return at the outmost
	 */
	protected int getSQLResultSetLimit(String schema, String name, HttpServletRequest request) {
		return 5000;
	}

	protected List<String> getTableNames(Connection conn, String schema) throws SQLException {
		String sql = "select distinct table_name from information_schema.tables where table_schema = ?";
		ArrayList<String> tableNames = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, schema);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					tableNames.add((String) rs.getObject("TABLE_NAME"));
				}
			}
			return tableNames;
		}
	}

	public static String createSQL(ODataIdentifier identifier, CharSequence projection, CharSequence where, CharSequence orderby, Integer skip, Integer top, ODataSchema table) {
		if (top == null) {
			top = 5000;
		}
		StringBuilder sql = new StringBuilder("select ");
		sql.append(projection);
		sql.append(" from ").append(identifier.getIdentifier());
		if (where != null && where.length() != 0) {
			sql.append(" where ");
			sql.append(where);
		}
		if (orderby != null) {
			sql.append(" order by ").append(orderby);
		}
		sql.append(" limit ").append(top);
		if (skip != null) {
			sql.append(" offset ").append(skip);
		}
		return sql.toString();
	}
}