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
import io.rtdi.appcontainer.odata.entity.definitions.EntitySets;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.metadata.Metadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

/**
 * The class with all OData operations where the OData endpoint is exposing a single table/view.
 *
 */
public abstract class JDBCoDataService extends JDBCoDataBase {
	public static final String ROWID = "__ROWID";

	@Operation(
			summary = "OData list of EntitySets",
			description = "Get the list of all EntitySets of this service",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "All OData EntitySets",
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
   	 	    		example = "json"
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
			summary = "OData $metadata",
			description = "The $metadata document describing the service",
			responses = {
					@ApiResponse(
	                    responseCode = "200",
	                    description = "The OData $metadata document about this service",
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
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name, ODataIdentifier.ENTITYNAME);
				Metadata ret = new Metadata();
				EntityType table = getMetadata(conn, identifier);
				ret.addObject(table);
				return createResponse(200, ret, format, request);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	@Operation(
			summary = "OData EntitySet",
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
   	  	    		description = "An OData filter condition",
   	  	    		example = ""
   	  	    		)
     		String filter,
   		 	@Parameter(
   	    		description = "An OData order by clause",
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
   	  	    		example = "0"
   	  	    		)
     		Integer skip,
   		 	@Parameter(
   	  	    		description = "Skip token for pagination",
   	  	    		example = ""
   	  	    		)
     		String skiptoken,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			String schema = ODataUtils.decodeName(schemaraw);
			String name = ODataUtils.decodeName(nameraw);
			ODataIdentifier identifier = createODataIdentifier(schema, name, ODataIdentifier.ENTITYNAME);
			return getEntitySetImpl(schemaraw, nameraw, select, filter, order, top, skip, skiptoken, format, identifier);
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	protected Response getEntitySetImpl(String schemaraw, String nameraw, String select, String filter, String order, Integer top,
			Integer skip, String skiptoken, String format, ODataIdentifier identifier)
			throws ODataException, SQLException, ServletException {
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
		if (skip == null && top != null) {
			resultsetlimit = top;
		} else {
			if (request.getHeader("resultsetlimit") != null) {
				try {
					resultsetlimit = Integer.valueOf(request.getHeader("resultsetlimit"));
				} catch (NumberFormatException e) {
					resultsetlimit = 5000;
				}
			}
		}
		int hardlimit = this.getSQLResultSetLimit(identifier.getDBSchema(), identifier.getDBObjectName(), request);
		if (resultsetlimit > hardlimit) {
			resultsetlimit = hardlimit;
		}
		AsyncResultSet query = null;
		if (skiptoken != null) {
			// Case 1: The client asked for the next page using the skiptoken, the server must provide that
			try {
				String resultsetid = String.valueOf(AsyncResultSet.tokenToResultsetId(skiptoken));
				query = getCachedResultSet(request, resultsetid);
				if (query == null) {
					throw new ODataException("The nextLink/skiptoken is no longer valid");
				} else {
					ODataResultSet ret = query.fetchRecords(skip, top, AsyncResultSet.tokenToPageId(skiptoken), format);
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
				String signature = schemaraw + nameraw + select + filter + order + resultsetlimit; 
				resultsetid = String.valueOf(signature.hashCode());
			}
			if (skip == null || skip == 0) {
			// Case 2: The client wants to have new data, so a new query must be executed
				query = new AsyncResultSetQuery(getConnection(), identifier, select, filter, order, resultsetid, maxpagesize, resultsetlimit, this);
				addToResultSetCache(resultsetid, query);
			} else {
				// Case 3: First call queried the first 100 rows, now the next 100 rows are requested
				query = getCachedResultSet(request, resultsetid);
				if (query == null) {
					// No such query is in the cache - no other option than to execute it again
					query = new AsyncResultSetQuery(getConnection(), identifier, select, filter, order, resultsetid, maxpagesize, resultsetlimit, this);
					addToResultSetCache(resultsetid, query);
				}
			}
			ODataResultSet ret = query.fetchRecords(skip, top, null, format);
			return createResponse(200, ret, format, request);
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
			summary = "Select a single OData Entity",
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
   	 	    		example = "PUBLIC"
   	 	    		)
    		String keys,
   		 	@Parameter(
   	 	    		description = "The list of columns to return, the projection in more general terms",
   	 	    		example = "USERNAME, ACTIVE"
   	 	    		)
    		String select,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name, ODataIdentifier.ENTITYNAME);
				return getODataEntityRowImpl(keys, select, format, conn, identifier);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	protected Response getODataEntityRowImpl(String keys, String select, String format, Connection conn,
			ODataIdentifier identifier) throws SQLException, ODataException {
		EntityType table = getMetadata(conn, identifier);
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
						row.put(columnnames[i], ODataTypes.convert(rs.getObject(i+1), JDBCType.valueOf(rs.getMetaData().getColumnType(i+1)), rs.getMetaData().getColumnTypeName(i+1)));
					}
					ret.addRow(row);
				}
				return createResponse(200, ret, format, request);
			}
		}
	}
	
	@Operation(
			summary = "OData EntitySet",
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
   	  	    		description = "An OData filter condition",
   	  	    		example = ""
   	  	    		)
     		String filter,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				String name = ODataUtils.decodeName(nameraw);
				ODataIdentifier identifier = createODataIdentifier(schema, name, ODataIdentifier.ENTITYNAME);
				return getODataEntitySetCountImpl(filter, format, conn, identifier);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	protected Response getODataEntitySetCountImpl(String filter, String format, Connection conn,
			ODataIdentifier identifier) throws SQLException, ODataException {
		EntityType table = getMetadata(conn, identifier);
		ODataFilterClause where = new ODataFilterClause(filter, table);
		String sql = createSQL(identifier, "count(*)", where.getSQL(), null, null, 1, table);
		try (PreparedStatement stmt = conn.prepareStatement(sql);) {
			try (ResultSet rs = stmt.executeQuery(); ) {
				if (rs.next()) {
					return createResponseText(200, rs.getInt(1), request);
				} else {
					return createResponse(200, Integer.valueOf(0), format, request);
				}
			}
		}
	}

}

