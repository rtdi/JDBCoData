package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.ResultSet;

import io.rtdi.appcontainer.odata.entity.ODataError;
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
import jakarta.ws.rs.core.Response;

/**
 * The variant where one OData endpoint exposes an entire database schema
 *
 */
public abstract class JDBCoDataServiceForSchema extends JDBCoDataService {

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
    public Response getODataEntitySetsForSchema(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				EntitySets ret = new EntitySets();
				try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), schema, null, null); ) {
					while (rs.next()) {
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
						String tablename = rs.getString(3);
						String tabletype = rs.getString(4);
						switch (tabletype) {
						case "TABLE":
						case "VIEW":
						// case "ALIAS":
						// case "SYNONYM":
						case "SYSTEM TABLE":
							ret.addTable(ODataUtils.encodeName(tablename));
							break;
						default:
							break;
						}
					}
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
    public Response getODataMetadataForSchema(
   		 	@Parameter(
 	    		description = "schemaname",
 	    		example = "INFORMATION_SCHEMA"
 	    		)
    		String schemaraw,
   		 	@Parameter(
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
			try (Connection conn = getConnection();) {
				String schema = ODataUtils.decodeName(schemaraw);
				Metadata ret = new Metadata();
				try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), schema, null, null); ) {
					while (rs.next()) {
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
						String tablename = rs.getString(3);
						String tabletype = rs.getString(4);
						switch (tabletype) {
						case "TABLE":
						case "VIEW":
						// case "ALIAS":
						// case "SYNONYM":
						case "SYSTEM TABLE":
							String name = ODataUtils.encodeName(tablename);
							ODataIdentifier identifier = createODataIdentifier(schema, name);
							EntityType table = getMetadata(conn, identifier);
							ret.addObject(table);
							break;
						default:
							break;
						}
					}
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
    public Response getODataEntitySetForSchema(
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
			ODataIdentifier identifier = createODataIdentifier(schema, name);
			return getEntitySetImpl(schemaraw, nameraw, select, filter, order, top, skip, skiptoken, format, identifier);
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
    public Response getODataEntityRowForSchema(
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
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				return getODataEntityRowImpl(keys, select, format, conn, identifier);
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
    public Response getODataEntitySetCountForSchema(
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
				ODataIdentifier identifier = createODataIdentifier(schema, name);
				return getODataEntitySetCountImpl(filter, format, conn, identifier);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

}

