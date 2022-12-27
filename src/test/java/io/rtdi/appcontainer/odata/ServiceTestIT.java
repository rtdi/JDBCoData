package io.rtdi.appcontainer.odata;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServiceTestIT {

	private static Path basedir;
	private static Tomcat tomcat;

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

	@Test
	void test() {
		fail("Not yet implemented");
	}

}
