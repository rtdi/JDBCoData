package io.rtdi.appcontainer.odata;

import java.util.Collections;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
public class TestService {

	@GET
	@Path("/{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getById(@PathParam("id") String id) {
		return Response.ok().entity(Collections.singletonList("Hello")).build();
	}
}