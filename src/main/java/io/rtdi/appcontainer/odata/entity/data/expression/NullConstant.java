package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public class NullConstant extends Expression {

	public NullConstant(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
	}

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public CharSequence getSQL() throws ODataException {
		return "null";
	}

}
