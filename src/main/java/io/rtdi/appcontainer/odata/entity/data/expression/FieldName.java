package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.ODataUtils;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.definitions.EntityTypeProperty;

public class FieldName extends Expression {

	private String name;

	public FieldName(Stack<Expression> stack, String name, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
		this.name = name;
	}
	
	@Override
	protected void parse(CharBuffer in) throws ODataException {
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public CharSequence getSQL() {
		return '"' + ODataUtils.decodeName(name) + "\" ";
	}

	public EntityTypeProperty getDataType() {
		return table.getPropertyMetadata(name);
	}

}
