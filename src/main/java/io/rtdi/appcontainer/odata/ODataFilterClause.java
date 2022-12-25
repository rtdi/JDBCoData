package io.rtdi.appcontainer.odata;

import java.nio.CharBuffer;
import java.util.ArrayList;

import io.rtdi.appcontainer.odata.entity.data.expression.Filter;
import io.rtdi.appcontainer.odata.entity.data.expression.IParameterValue;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public class ODataFilterClause extends ODataSQLStatementPart {

	public ODataFilterClause(String text, ODataSchema table) throws ODataException {
		if (table == null) {
			throw new ODataException("No table metadata provided to the API call");			
		} else if (table.getEntityType() == null) {
			throw new ODataException("The table metadata has no details about the EntityType");
		} else if (text != null) {
			CharBuffer in = CharBuffer.wrap(text);
			Filter f = new Filter(table);
			f.parse(in);
			sql = new StringBuilder(f.getSQL());
			if (paramValues == null && f.getParams() != null) {
				paramValues = new ArrayList<>();
				for (IParameterValue e : f.getParams()) {
					paramValues.add(e.getValue());
				}
			}
		} else {
			sql = null;
		}
	}

}
