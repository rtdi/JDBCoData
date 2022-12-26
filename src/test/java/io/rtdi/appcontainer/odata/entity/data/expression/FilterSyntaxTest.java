package io.rtdi.appcontainer.odata.entity.data.expression;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.CharBuffer;
import java.sql.JDBCType;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.rtdi.appcontainer.odata.ODataIdentifier;
import io.rtdi.appcontainer.odata.ODataTypes;
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

	@Test
	void test1() {
		try {
			CharBuffer in = CharBuffer.wrap("FirstName eq 'Scott' or FirstName eq 'Fitz'");
			ODataSchema table1 = new ODataSchema(new ODataIdentifier("schema", "table1"), "TABLE");
			table1.getEntityType().addColumn("FirstName", JDBCType.VARCHAR, ODataTypes.STRING.name(), 255, 0);
			Filter f = new Filter(table1);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void test2() {
		try {
			CharBuffer in = CharBuffer.wrap("ACCOUNTID eq '0011j00001KwLiuAAF' and (ID ne '8011j000007S0mZAAS' and ID ne '8011j000007S0llAAC' and ID ne '8011j000007S0mAAAS' and ID ne '8011j000007S0nNAAS' and ID ne '8011j000007S0myAAC' and ID ne '8011j000007S0nmAAC' and ID ne '8011j000007S0oBAAS' and ID ne '8011j000007S0oaAAC')");
			ODataSchema table2 = new ODataSchema(new ODataIdentifier("schema", "table1"), "TABLE");
			table2.getEntityType().addColumn("ACCOUNTID", JDBCType.VARCHAR, ODataTypes.STRING.name(), 255, 0);
			table2.getEntityType().addColumn("ID", JDBCType.VARCHAR, ODataTypes.STRING.name(), 255, 0);
			Filter f = new Filter(table2);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void test3() {
		try {
			CharBuffer in = CharBuffer.wrap("ACCOUNTID eq '0011j00001KwLiuAAF' and ID ne '8011j000007S0mZAAS'");
			ODataSchema table2 = new ODataSchema(new ODataIdentifier("schema", "table1"), "TABLE");
			table2.getEntityType().addColumn("ACCOUNTID", JDBCType.VARCHAR, ODataTypes.STRING.name(), 255, 0);
			table2.getEntityType().addColumn("ID", JDBCType.VARCHAR, ODataTypes.STRING.name(), 255, 0);
			Filter f = new Filter(table2);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
