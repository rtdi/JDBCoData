package io.rtdi.appcontainer.odata.entity.data.expression;

import static org.junit.jupiter.api.Assertions.fail;

import java.nio.CharBuffer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

class FilterSyntaxTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	void test() {
		try {
			CharBuffer in = CharBuffer.wrap("FirstName eq 'Scott' or FirstName eq 'Fitz'");
			Filter f = new Filter(null);
			f.parse(in);
			System.out.println(f.getExpression());
			System.out.println(f.getSQL());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
