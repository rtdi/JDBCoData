package io.rtdi.appcontainer.odata;

import io.rtdi.appcontainer.odata.entity.ODataError;
import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.data.ODataResultSet;
import io.rtdi.appcontainer.odata.entity.metadata.EntitySets;
import io.rtdi.appcontainer.odata.entity.metadata.Metadata;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

public abstract class JDBCoDataService extends JDBCoDataBase {

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
    		String schema_raw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection()) {
				String schema = ODataUtils.decodeName(schema_raw);
				EntitySets ret = new EntitySets();
				for (String tableName: getTableNames(conn, schema)) {
					ret.addTable(tableName);
				}
				return createResponse(200, ret, format, request);
			}
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
    		String schema_raw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schema_raw);
				Metadata ret = new Metadata();
				for (String tableName: getTableNames(conn, schema)) {
					ODataIdentifier identifier = createODataIdentifier(schema, tableName);
					ODataSchema table = getMetadata(conn, identifier);
					ret.addObject(table);
				}
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
    		String schema_raw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String name_raw,
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
			String schema = ODataUtils.decodeName(schema_raw);
			String name = ODataUtils.decodeName(name_raw);
			int hardlimit = this.getSQLResultSetLimit(schema, name, request);
			if (resultsetlimit > hardlimit) {
				resultsetlimit = hardlimit;
			}
			ODataIdentifier identifier = createODataIdentifier(schema, name);
			AsyncResultSet query = null;
			if (skiptoken != null) {
				// Case 1: The client asked for the next page using the skiptoken, the server must provide that
				try {
					String resultsetid = String.valueOf(AsyncResultSet.tokenToResultsetid(skiptoken));
					query = getCachedResultSet(resultsetid);
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
					String signature = schema_raw + name_raw + select + filter + order;
					resultsetid = String.valueOf(signature.hashCode());
				}
				if (skip == null || skip == 0) {
				// Case 2: The client wants to have new data, so a new query must be executed
					query = new AsyncResultSetQuery(getConnection(), identifier, select, filter, order, resultsetid, maxpagesize, resultsetlimit, this);
					addToResultSetCache(resultsetid, query);
				} else {
					// Case 3: First call queried the first 100 rows, now the next 100 rows are requested
					query = getCachedResultSet(resultsetid);
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
    		String name_raw,
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
				String name = ODataUtils.decodeName(name_raw);
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				ODataSchema table = getMetadata(conn, identifier);
				ODataKeyClause where = new ODataKeyClause(keys, table);
				ODataSelectClause projection = new ODataSelectClause(select, table);
				String sql = createSQL(identifier, projection.getSQL(), where.getSQL(), null, null, 1, table);
				try (PreparedStatement stmt = conn.prepareStatement(sql);) {
					where.setPreparedStatementParameters(stmt);
					try (ResultSet rs = stmt.executeQuery(); ) {
						ODataResultSet ret = new ODataResultSet(identifier);
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
    		String schema_raw,
   		 	@Parameter(
 	    		description = "objectname",
 	    		example = "USERS"
 	    		)
    		String name_raw,
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
				String schema = ODataUtils.decodeName(schema_raw);
				String name = ODataUtils.decodeName(name_raw);
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

	protected Response healthCheck() {
		try (Connection conn = getConnection()) {
			String sql = "select 1";
			Statement stmt = conn.createStatement();
			stmt.execute(sql);
			return createResponse(200, request);
		} catch (Exception e) {
			return createResponse(501, request);
		}
	}

	protected static Response createResponse(int httpStatus, HttpServletRequest request) {
		Response.ResponseBuilder r = Response.status(httpStatus).header("OData-Version", ODataUtils.VERSIONVALUE);
		r = r.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
		return r.build();
	}

	protected static Response createResponse(int httpStatus, Object entity, String format, HttpServletRequest request) {
		Response.ResponseBuilder r = Response.status(httpStatus).entity(entity).header("OData-Version", ODataUtils.VERSIONVALUE);
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
}
