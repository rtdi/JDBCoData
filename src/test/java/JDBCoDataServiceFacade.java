

import java.sql.Connection;
import java.sql.SQLException;

import io.rtdi.appcontainer.odata.JDBCoDataService;
import io.rtdi.appcontainer.odata.ODataIdentifier;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/odata")
public class JDBCoDataServiceFacade extends JDBCoDataService {

	@Override
	protected Connection getConnection() throws SQLException, ServletException {
		throw new SQLException("The getConnection() method has not been implemented yet");
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

}
