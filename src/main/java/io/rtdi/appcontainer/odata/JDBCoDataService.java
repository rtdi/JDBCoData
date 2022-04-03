package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import io.rtdi.appcontainer.odata.entity.ODataError;
import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.data.ODataResultSet;
import io.rtdi.appcontainer.odata.entity.metadata.EntitySets;
import io.rtdi.appcontainer.odata.entity.metadata.EntityTypeProperty;
import io.rtdi.appcontainer.odata.entity.metadata.Metadata;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

public abstract class JDBCoDataService extends JDBCoDataBase {
	public static final String ROWID = "__ROWID";

	@Operation(
			summary = "oData list of EntitySets",
			description = "Get the list of all EntitySets of this service",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "All oData EntitySets",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = EntitySets.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "500", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ODataError.class)
		                            )
		                    }
					)
            })
	@Tag(name = "ReadDB")
    public Response getODataEntitySets(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String nameraw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "$format=json"
   	 	    		)
    		String format
    		) {
		try {
				EntitySets ret = new EntitySets();
				ret.addTable("TABLE");
				return createResponse(200, ret, format, request);
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}
	
	@Operation(
			summary = "oData $metadata",
			description = "The $metadata document describing the service",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The oData $metadata document about this service",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = Metadata.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "500", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ODataError.class)
		                            )
		                    }
					)
            })
	@Tag(name = "ReadDB")
    public Response getODataMetadata(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String nameraw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "$format=json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				Metadata ret = new Metadata();
				ODataSchema table = getMetadata(conn, identifier);
				ret.addObject(table);
				return createResponse(200, ret, format, request);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	@Operation(
			summary = "oData EntitySet",
			description = "Read the data of a table",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The resultset of the table",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = ODataResultSet.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "500", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ODataError.class)
		                            )
		                    }
					)
            })
	@Tag(name = "ReadDB")
    public Response getODataEntitySet(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String nameraw,
   		 	@Parameter(
   	 	    		description = "The list of columns to return, the projection in more general terms",
   	 	    		example = "USERNAME, ACTIVE"
   	 	    		)
    		String select,
   		 	@Parameter(
   	  	    		description = "An oData filter condition",
   	  	    		example = "USERNAME = 'ABC'"
   	  	    		)
     		String filter,
   		 	@Parameter(
   	    		description = "An oData order by clause",
   	    		example = "USERNAME"
   	    		)
      		String order,
   		 	@Parameter(
   	    		description = "Limit to n many records",
   	    		example = "100"
   	    		)
      		Integer top,
   		 	@Parameter(
   	  	    		description = "Skip the first n records for pagination",
   	  	    		example = "400"
   	  	    		)
     		Integer skip,
   		 	@Parameter(
   	  	    		description = "Skip token for pagination",
   	  	    		example = "Server produced value"
   	  	    		)
     		String skiptoken,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "$format=json"
   	 	    		)
    		String format
    		) {
		try {
			Integer maxpagesize = 5000;
			// Prefer: odata.track-changes,odata.maxpagesize=3
			Enumeration<String> preferences = request.getHeaders("Prefer");
			if (preferences != null) {
				while (preferences.hasMoreElements()) {
					String value = preferences.nextElement();
					if (value.startsWith("odata.maxpagesize")) {
						int p = value.indexOf('=');
						if (p != -1) {
							try {
								maxpagesize = Integer.valueOf(value.substring(p+1));
							} catch (NumberFormatException e) {
								// to be ignored
							}
						}
					}
				}
			}
			int resultsetlimit = 5000;
			if (request.getHeader("resultsetlimit") != null) {
				try {
					resultsetlimit = Integer.valueOf(request.getHeader("resultsetlimit"));
					if (resultsetlimit < 1000) {
						resultsetlimit = 1000;
					}
				} catch (NumberFormatException e) {
					resultsetlimit = 5000;
				}
			}
			String schema = ODataUtils.decodeName(schemaraw);
			String name = ODataUtils.decodeName(nameraw);
			int hardlimit = this.getSQLResultSetLimit(schema, name, request);
			if (resultsetlimit > hardlimit) {
				resultsetlimit = hardlimit;
			}
			ODataIdentifier identifier = createODataIdentifier(schema, name);
			AsyncResultSet query = null;
			if (skiptoken != null) {
				// Case 1: The client asked for the next page using the skiptoken, the server must provide that
				try {
					String resultsetid = String.valueOf(AsyncResultSet.tokenToresultsetid(skiptoken));
					query = getResultSetCache(request, resultsetid);
					if (query == null) {
						throw new ODataException("The nextLink/skiptoken is no longer valid");
					} else {
						ODataResultSet ret = query.fetchRecords(skip, top, AsyncResultSet.tokenToPageid(skiptoken));
						return createResponse(200, ret, format, request);
					}
				} catch (NumberFormatException e) {
					throw new ODataException("Invalid $skiptoken");
				}
			} else {
				String resultsetid = request.getHeader("SAP-ContextId");
				if (resultsetid == null) {
					resultsetid = request.getHeader("ContextId");
				}
				if (resultsetid == null) {
					String signature = schemaraw + nameraw + select + filter + order; 
					resultsetid = String.valueOf(signature.hashCode());
				}
				if (skip == null || skip == 0) {
				// Case 2: The client wants to have new data, so a new query must be executed
					query = new AsyncResultSetQuery(getConnection(), identifier, select, filter, order, resultsetid, maxpagesize, resultsetlimit, this);
					addToResultSetCache(resultsetid, query);
				} else {
					// Case 3: First call queried the first 100 rows, now the next 100 rows are requested
					query = getResultSetCache(request, resultsetid);
					if (query == null) {
						// No such query is in the cache - no other option than to execute it again
						query = new AsyncResultSetQuery(getConnection(), identifier, select, filter, order, resultsetid, maxpagesize, resultsetlimit, this);
						addToResultSetCache(resultsetid, query);
					}
				}
				ODataResultSet ret = query.fetchRecords(skip, top, null);
				return createResponse(200, ret, format, request);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
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

	@Operation(
			summary = "Select a single oData Entity",
			description = "Read one row of a table based on its key",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The resultset of the row",
	                    content = {
	                            @Content(
	                                    schema = @Schema(implementation = ODataResultSet.class)
	                            )
	                    }
                    ),
					@ApiResponse(
							responseCode = "500", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ODataError.class)
		                            )
		                    }
					)
            })
	@Tag(name = "ReadDB")
    public Response getODataEntityRow(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String nameraw,
   		 	@Parameter(
   	 	    		description = "A text with either a single value - in case the primary key consists of a single column only - or comma separated list of key=value components",
   	 	    		example = "OrderId=1234,ProductId=1234"
   	 	    		)
    		String keys,
   		 	@Parameter(
   	 	    		description = "The list of columns to return, the projection in more general terms",
   	 	    		example = "USERNAME, ACTIVE"
   	 	    		)
    		String select,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "$format=json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				ODataSchema table = getMetadata(conn, identifier);
				ODataKeyClause where = new ODataKeyClause(keys, table);
				ODataSelectClause projection = new ODataSelectClause(select, table);
				String sql = createSQL(identifier, projection.getSQL(), where.getSQL(), null, null, 1, table);
				try (PreparedStatement stmt = conn.prepareStatement(sql);) {
					where.setPreparedStatementParameters(stmt);
					try (ResultSet rs = stmt.executeQuery(); ) {
						ODataResultSet ret = new ODataResultSet();
						String[] columnnames = new String[rs.getMetaData().getColumnCount()];
						for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
							columnnames[i] = ODataUtils.encodeName(rs.getMetaData().getColumnName(i+1));
						}
						while (rs.next()) {
							ODataRecord row = new ODataRecord();
							for (int i=0; i<columnnames.length; i++) {
								row.put(columnnames[i], ODataTypes.convert(rs.getObject(i+1)));
							}
							ret.addRow(row);
						}
						return createResponse(200, ret, format, request);
					}
				}
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}
	
	@Operation(
			summary = "oData EntitySet",
			description = "Read the data of a table",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The resultset of the table",
	                    content = {
	                            @Content( schema = @Schema(type = "int"))
	                    }
                    ),
					@ApiResponse(
							responseCode = "500", 
							description = "Any exception thrown",
		                    content = {
		                            @Content(
		                                    schema = @Schema(implementation = ODataError.class)
		                            )
		                    }
					)
            })
	@Tag(name = "ReadDB")
    public Response getODataEntitySetCount(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String nameraw,
   		 	@Parameter(
   	  	    		description = "An oData filter condition",
   	  	    		example = "USERNAME = 'ABC'"
   	  	    		)
     		String filter,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "$format=json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				ODataSchema table = getMetadata(conn, identifier);
				ODataFilterClause where = new ODataFilterClause(filter, table);
				String sql = createSQL(identifier, "count(*)", where.getSQL(), null, null, 1, table);
				try (PreparedStatement stmt = conn.prepareStatement(sql);) {
					try (ResultSet rs = stmt.executeQuery(); ) {
						if (rs.next()) {
							return createResponse(200, rs.getInt(1), format, request);
						} else {
							return createResponse(200, Integer.valueOf(0), format, request);
						}
					}
				}
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	public static String createSQL(ODataIdentifier identifer, CharSequence projection, CharSequence where, CharSequence orderby, Integer skip, Integer top, ODataSchema table) {
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
		sql.append(" limit ").append(top);
		if (skip != null) {
			sql.append(" offset ").append(skip);
		}
		return sql.toString();
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
}

