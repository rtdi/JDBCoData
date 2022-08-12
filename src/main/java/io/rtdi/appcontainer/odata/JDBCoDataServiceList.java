package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;

import io.rtdi.appcontainer.odata.entity.ODataError;
import io.rtdi.appcontainer.odata.entity.data.ODataRecord;
import io.rtdi.appcontainer.odata.entity.data.ODataResultSet;
import io.rtdi.appcontainer.odata.entity.metadata.EntityContainer;
import io.rtdi.appcontainer.odata.entity.metadata.EntityType;
import io.rtdi.appcontainer.odata.entity.metadata.Metadata;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

public abstract class JDBCoDataServiceList extends JDBCoDataBase {

	public static final String TABLELIST = "___TABLELIST";
	@Context
    protected Configuration configuration;

	@Context 
	protected ServletContext servletContext;
	
	@Context 
	protected HttpServletRequest request;

	private ODataIdentifier identifier = new ODataIdentifier("PUBLIC", TABLELIST);
	
	private static ODataSchema schema;

	@Operation(
			summary = "oData service to get all oData services",
			description = "Get the list of all oData services",
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
			AsyncResultSet resultset = JDBCoDataService.getResultSetCache(request, TABLELIST);
			if (resultset == null) {
				resultset = new AsyncResultSetStatic(getConnection(), identifier , TABLELIST, 5000, 5000, this);
				try (Connection conn = resultset.conn;) {
					try (ResultSet rs = conn.getMetaData().getTables(conn.getCatalog(), null, null, null);) {
						while (rs.next()) {
							String tabletype = rs.getString(4);
							ODataRecord rec;
							switch (tabletype) {
							case "TABLE":
							case "VIEW":
							case "ALIAS":
							case "SYNONYM":
							case "SYSTEM TABLE":
								rec = new ODataRecord();
								rec.put("SCHEMANAME", rs.getString(2));
								rec.put("OBJECTNAME", rs.getString(3));
								rec.put("COMMENT", rs.getString(5));
								rec.put("OBJECTTYPE", tabletype);
								rec.put("URL", ODataUtils.encodeName(rs.getString(2)) + "/" + ODataUtils.encodeName(rs.getString(3)));
								resultset.rows.add(rec);
								break;
							}
						}
						resultset.readcompleted = true;
					}
				}
				addToResultSetCache(TABLELIST, resultset);
			}
			if (skiptoken != null) {
				// Case 1: The client asked for the next page using the skiptoken, the server must provide that
				try {
					ODataResultSet ret = resultset.fetchRecords(skip, top, AsyncResultSet.tokenToPageid(skiptoken));
					return createResponse(200, ret, format, request);
				} catch (NumberFormatException e) {
					throw new ODataException("Invalid $skiptoken");
				}
			} else {
				ODataResultSet ret = resultset.fetchRecords(skip, top, null);
				return createResponse(200, ret, format, request);
			}
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}

	@Override
	protected ODataSchema readTableMetadata(Connection conn, ODataIdentifier identifier)
			throws SQLException, ODataException {
		if (schema == null) {
			schema = new ODataSchema(identifier, getURL());
			EntityContainer container = new EntityContainer(identifier, "METADATA");
			EntityType entitytype = new EntityType(identifier);
			entitytype.addKey("SCHEMANAME");
			entitytype.addKey("OBJECTNAME");
			entitytype.addColumn("SCHEMANAME", JDBCType.VARCHAR, null, 256, null);
			entitytype.addColumn("OBJECTNAME", JDBCType.VARCHAR, null, 256, null);
			entitytype.addColumn("COMMENT", JDBCType.VARCHAR, null, null, null);
			entitytype.addColumn("OBJECTTYPE", JDBCType.VARCHAR, null, 30, null);
			entitytype.addColumn("URL", JDBCType.VARCHAR, null, 1024, null);
			schema.setContainer(container);
			schema.setEntityType(entitytype);
		}
		return schema;
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
   	 	    		description = "Optional parameter to overrule the format",
   	 	    		example = "json"
   	 	    		)
    		String format
    		) {
		try {
				Metadata ret = new Metadata();
				ODataSchema table = readTableMetadata(null, identifier);
				ret.addObject(table);
				return createResponse(200, ret, format, request);
		} catch (Exception e) {
			ODataError error = new ODataError(e);
			return createResponse(error.getStatusCode(), error, format, request);
		}
	}
}
