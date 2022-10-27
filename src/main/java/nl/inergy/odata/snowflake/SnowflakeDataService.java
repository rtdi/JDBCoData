package nl.inergy.odata.snowflake;

import io.rtdi.appcontainer.odata.JDBCoDataService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import nl.inergy.odata.service.Roles;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Path("/data")
public class SnowflakeDataService extends JDBCoDataService {

    @Override
    protected Connection getConnection() throws SQLException, NamingException {
        // resource injection does not seem to work, so do the lookup ourselves
        InitialContext ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup("java:app/jdbc/snowflakeSource");
        return ds.getConnection();
    }

    @GET
    @Path("/health_check")
    @Produces({MediaType.TEXT_PLAIN})
    public Response healthCheck() {
        return super.healthCheck();
    }

    @Override
    @GET
    @Path("/tables/{schema}")
    @RolesAllowed(Roles.odataRole)
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
    @RolesAllowed(Roles.odataRole)
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
    @RolesAllowed(Roles.odataRole)
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
    @RolesAllowed(Roles.odataRole)
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
    @RolesAllowed(Roles.odataRole)
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
