import io.rtdi.appcontainer.odata.JDBCoDataService;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;

@Path("/odata")
public class JDBCoDataServiceFacade extends JDBCoDataService {

	@Override
	protected Connection getConnection() throws SQLException, ServletException {
		throw new SQLException("The getConnection() method has not been implemented yet");
	}

	@Override
	@GET
	@Path("/tables/{schema}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySets(
    		@PathParam("schema")
    		String schema_raw,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySets(schema_raw, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/$metadata")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getODataMetadata(
    		@PathParam("schema")
    		String schema_raw,
			@QueryParam("$format")
    		String format
			) {
		return super.getODataMetadata(schema_raw, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySet(
    		@PathParam("schema")
    		String schema_raw,
    		@PathParam("name")
    		String name_raw,
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
		return super.getODataEntitySet(schema_raw, name_raw, select, filter, order, top, skip, skiptoken, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}({keys})")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntityRow(
    		@PathParam("schema")
    		String schema_raw,
    		@PathParam("name")
    		String name_raw,
    		@PathParam("keys")
    		String keys,
    		@QueryParam("$select")
    		String select,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntityRow(schema_raw, name_raw, keys, select, format);
	}

	@Override
	@GET
	@Path("/tables/{schema}/{name}/$count")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySetCount(
    		@PathParam("schema")
    		String schema_raw,
    		@PathParam("name")
    		String name_raw,
    		@QueryParam("$filter")
    		String filter,
    		@QueryParam("$format")
    		String format) {
		return super.getODataEntitySetCount(schema_raw, name_raw, filter, format);
	}
}
