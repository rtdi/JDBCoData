package io.rtdi.appcontainer.odata.entity.data.expression;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.CharBuffer;
import java.sql.JDBCType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.rtdi.appcontainer.odata.entity.metadata.EntityType;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

class FilterSyntaxTest {

	private static ODataSchema schema;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		schema = new ODataSchema();
		
		EntityType entitytype = new EntityType();
		entitytype.addColumn("FirstName", JDBCType.NVARCHAR, "NVARCHAR", 100, null);
		schema.setEntityType(entitytype );
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@Test
	void test() {
		try {
			CharBuffer in = CharBuffer.wrap("FirstName eq 'Scott' or (FirstName eq 'Fitz')");
			Filter f = new Filter(schema);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testContains() {
		try {
			CharBuffer in = CharBuffer.wrap("contains(FirstName,'Alf')");
			Filter f = new Filter(schema);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
