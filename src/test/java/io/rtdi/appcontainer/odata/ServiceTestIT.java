package io.rtdi.appcontainer.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class ServiceTestIT {

	private static Path basedir;
	private static Tomcat tomcat;
	private static HttpClient client;
	private ObjectMapper om = new ObjectMapper();

	@BeforeAll
	static void setUp() throws Exception {
        tomcat = new Tomcat();
        basedir = Files.createTempDirectory("tomcat");
        tomcat.setBaseDir(basedir.toString());
        tomcat.setPort(8080);
        tomcat.getConnector();
        Context context = tomcat.addWebapp("", basedir.toString());
        Tomcat.addServlet(context, "odata-servlet",
           new ServletContainer(new ResourceConfig(WebApplication.class)));
        
        tomcat.start();
        Thread.sleep(1000L);
        CookieHandler.setDefault(new CookieManager());
		client = HttpClient.newBuilder().cookieHandler(CookieHandler.getDefault()).build();
	}

	@AfterAll
	static void tearDown() throws Exception {
		if (tomcat != null) {
			tomcat.stop();
		}
		if (basedir != null) {
			Files.walk(basedir)
		      .sorted(Comparator.reverseOrder())
		      .map(Path::toFile)
		      .forEach(File::delete);
		}
	}

	@ParameterizedTest
	@MethodSource("getTestData")
	public void testTablesEndpoint(String url) {
		try {
			HttpResponse<String> response = callURL(url);
			validateResponse(url, response);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	private Map<String, Object> validateResponse(String url, HttpResponse<String> response)
			throws IOException, JsonProcessingException, JsonMappingException {
		String filesuffix;
		if (url.contains("$format=xml")) {
			filesuffix = ".xml";
		} else {
			filesuffix = ".json";
		}
		String expectedfile = "src/test/resources/responses/" + 
				url
					.replace("/", "_")
					.replace("?$format=json", "")
					.replace("&$format=json", "")
					.replace("?$format=xml", "")
					.replace("&$format=xml", "")
					.replace("?", "_")
					.replace("$", "_")
					.replace("&", "_") +
					filesuffix;
		Path p = Path.of(expectedfile);
		if (!p.toFile().exists()) {
			Files.writeString(p, response.body());
		}
		String expected = Files.readString(p);
		HttpHeaders headers = response.headers();
		Optional<String> mime = headers.firstValue("Content-Type");
		String mimetype = mime.get();
		Map<String, Object> responseobj;
		Map<String, Object> expectedobj;
		if (mimetype.equals("application/json")) {
			responseobj = om.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
			expectedobj = om.readValue(expected, new TypeReference<Map<String, Object>>() {});
			assertEquals(expectedobj, responseobj);
			return responseobj;
		} else {
			return null;
		}
	}
	
	@Test
	public void testClientsidePagination() {
		try {
			String urlformat = "tables/user/deniro/RS?$skip=%s&$top=%s&$format=json";
			String url = String.format(urlformat, "0", "10");
			HttpResponse<String> response = callURL(url);
			validateResponse(url, response);
			url = String.format(urlformat, "10", "10");
			response = callURL(url);
			validateResponse(url, response);
			url = String.format(urlformat, "20", "10");
			response = callURL(url);
			validateResponse(url, response);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}
	
	@Test
	public void testServersidePagination() {
		try {
			String url = "tables/user/deniro/RS?$filter=" + encode("Year gt '1996'") + "&$format=json";
			HttpResponse<String> response = callURL(url, "Prefer", "odata.maxpagesize=20");
			Map<String, Object> responseobj = validateResponse(url, response);
			assertNotNull(responseobj, "Response of first call is not parsed as valid");
			Object nextlink = responseobj.get("@odata.nextLink");
			assertNotNull(nextlink, "Nextlink not returned by first call");
			int count = 1;
			while (nextlink != null && count < 10) {
				url = nextlink.toString().replace("/api/odata/", "");
				response = callURL(url);
				responseobj = validateResponse(url, response);
				nextlink = responseobj.get("@odata.nextLink");
				count++;
			}
			assertEquals(3, count);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	
	private static String encode(String value) {
	    try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}


	private HttpResponse<String> callURL(String url, String ...headerpairs) throws IOException, InterruptedException {
		String fullurl = "http://localhost:8080/api/odata/" + url;
		System.out.println("calling url " + fullurl);
		HttpRequest request;
		if (headerpairs != null && headerpairs.length != 0) {
			request = HttpRequest.newBuilder()
				      .uri(URI.create(fullurl))
				      .headers(headerpairs)
				      .build();
		} else {
			request = HttpRequest.newBuilder()
				      .uri(URI.create(fullurl))
				      .build();
		}
		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IOException(String.format("http call %s returned status %d and body %s", url, response.statusCode(), response.body()));
		}
		System.out.println("Result");
		System.out.println(response.body());
		return response;
	}
	
	private static Stream<Arguments> getTestData() {
	    return Stream.of(
	      Arguments.of("tables/user/deniro/?$format=json"),
	      Arguments.of("tables/user/deniro/$metadata?$format=json"),
	      Arguments.of("tables/user/deniro/RS?$format=json"),
	      Arguments.of("tables/user/deniro/RS?$filter=" + encode("Year eq '1996'") + "&$format=json"),
	      Arguments.of("tables/user/deniro/RS?$orderby=" + encode("Score desc") + "&$format=json"),
	      Arguments.of("tables/user/deniro/RS?$orderby=" + encode("Score desc") + "&$top=5&$format=json"),
	      Arguments.of("tables/user/deniro/RS/$count?$format=json"),
	      Arguments.of("schemas/user/?$format=json"),
	      Arguments.of("schemas/user/$metadata?$format=json")
	    );
	}
	
}
