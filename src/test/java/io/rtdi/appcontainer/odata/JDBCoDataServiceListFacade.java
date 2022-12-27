package io.rtdi.appcontainer.odata;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/odata")
public class JDBCoDataServiceListFacade extends JDBCoDataServiceList {

	private String jdbcdriver;
	private String jdbcurl;
	private String dbuser;
	private String dbpasswd;

	public JDBCoDataServiceListFacade() throws ClassNotFoundException {
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
	@Path("/tables/$metadata")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
	public Response getODataMetadata(
			@QueryParam("$format")
    		String format
			) {
		return super.getODataMetadata(format);
	}

	@Override
	@GET
	@Path("/tables/" + ODataIdentifier.ENTITYSETNAME)
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
	public Response getODataEntitySet(
    		@QueryParam("$top")
    		Integer top,
    		@QueryParam("$skip")
    		Integer skip,
    		@QueryParam("$skiptoken")
    		String skiptoken,
    		@QueryParam("$format")
    		String format
			) {
		return super.getODataEntitySet(top, skip, skiptoken, format);
	}

}
