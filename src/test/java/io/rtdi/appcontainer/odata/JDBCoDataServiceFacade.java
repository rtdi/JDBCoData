package io.rtdi.appcontainer.odata;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * The oData rest endpoint supports two categories of endpoints<BR>
 * The odata/tables/schemaname/tablename/ endpoint where one endpoint consists of a single table only and <BR>
 * The odata/schemas/schemaname/ endpoint that contains all tables within a schema. <BR>
 * <BR>
 * The main differences is the size of the metadata document ant the entityname. In the first option the name is always DBOBJECT as
 * entity name, in the second variant the entityname is the table name itself. 
 *
 */
@Path("/odata")
public class JDBCoDataServiceFacade extends JDBCoDataServiceForSchema {
	
	private String jdbcdriver;
	private String jdbcurl;
	private String dbuser;
	private String dbpasswd;

	public JDBCoDataServiceFacade() throws ClassNotFoundException {
		jdbcdriver = System.getenv("JDBCDRIVER");
		jdbcurl = System.getenv("JDBCURL");
		dbuser = System.getenv("JDBCUSERNAME");
		dbpasswd = System.getenv("JDBCPASSWD");
		Class.forName(jdbcdriver);
	}

	@Override
	protected Connection getConnection() throws SQLException, ServletException {
        return DriverManager.getConnection(jdbcurl, dbuser, dbpasswd);                  
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySets(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySets(schemaraw, nameraw, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/$metadata")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getODataMetadata(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
			String nameraw,
			@QueryParam("$format")
    		String format
			) {
		return super.getODataMetadata(schemaraw, nameraw, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/" + ODataIdentifier.ENTITYSETNAME)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySet(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@QueryParam("$select")
    		String select,
    		@QueryParam("$filter")
    		String filter,
    		@QueryParam("$order")
    		String order,
    		@QueryParam("$top")
    		Integer top,
    		@QueryParam("$skip")
    		Integer skip,
    		@QueryParam("$skiptoken")
    		String skiptoken,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySet(schemaraw, nameraw, select, filter, order, top, skip, skiptoken, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/" + ODataIdentifier.ENTITYSETNAME + "({keys})")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntityRow(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@PathParam("keys")
    		String keys,
    		@QueryParam("$select")
    		String select,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntityRow(schemaraw, nameraw, keys, select, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/" + ODataIdentifier.ENTITYSETNAME + "/$count")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySetCount(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@QueryParam("$filter")
    		String filter,
    		@QueryParam("$format")
    		String format) {
		return super.getODataEntitySetCount(schemaraw, nameraw, filter, format);
	}

	
	@Override
	@GET
	@Path("/schemas/{schema}/")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySetsForSchema(
    		@PathParam("schema")
    		String schemaraw,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySetsForSchema(schemaraw, format);
	}

	@Override
	@GET
	@Path("/schemas/{schema}/$metadata")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getODataMetadataForSchema(
    		@PathParam("schema")
    		String schemaraw,
			@QueryParam("$format")
    		String format
			) {
		return super.getODataMetadataForSchema(schemaraw, format);
	}

	@Override
	@GET
	@Path("/schemas/{schema}/{name}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySetForSchema(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@QueryParam("$select")
    		String select,
    		@QueryParam("$filter")
    		String filter,
    		@QueryParam("$order")
    		String order,
    		@QueryParam("$top")
    		Integer top,
    		@QueryParam("$skip")
    		Integer skip,
    		@QueryParam("$skiptoken")
    		String skiptoken,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySetForSchema(schemaraw, nameraw, select, filter, order, top, skip, skiptoken, format);
	}

	@Override
	@GET
	@Path("/schemas/{schema}/{name}({keys})")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntityRowForSchema(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@PathParam("keys")
    		String keys,
    		@QueryParam("$select")
    		String select,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntityRowForSchema(schemaraw, nameraw, keys, select, format);
	}

	@Override
	@GET
	@Path("/schemas/{schema}/{name}/$count")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySetCountForSchema(
    		@PathParam("schema")
    		String schemaraw,
    		@PathParam("name")
    		String nameraw,
    		@QueryParam("$filter")
    		String filter,
    		@QueryParam("$format")
    		String format) {
		return super.getODataEntitySetCountForSchema(schemaraw, nameraw, filter, format);
	}

}
