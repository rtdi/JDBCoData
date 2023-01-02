package io.rtdi.appcontainer.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JdbcTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void test() {
		try {
			String jdbcurl = "jdbc:relique:csv:src/test/resources?separator=,&fileExtension=.csv&quotechar=\"";
			List<List<String>> data = new ArrayList<>();
			Connection conn = DriverManager.getConnection(jdbcurl);
			try (ResultSet rs = conn.getMetaData().getColumns(null, null, "deniro", null);) {
				while (rs.next()) {
					List<String> recordddata = new ArrayList<>();
					for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
						recordddata.add(rs.getString(i+1));
					}
				}
			}
			try (PreparedStatement stmt = conn.prepareStatement("select * from \"user\".\"deniro\""); ) {
				try (ResultSet rs = stmt.executeQuery();) {
					while (rs.next()) {
						List<String> recordddata = new ArrayList<>();
						for (int i=0; i<rs.getMetaData().getColumnCount(); i++) {
							recordddata.add(rs.getString(i+1));
						}
						data.add(recordddata);
					}
				}
			}
			assertEquals(87, data.size(), "Record count does not match");
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Not yet implemented");
		}
	}

}
