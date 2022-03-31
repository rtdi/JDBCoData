package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.ODataUtils;

public class FieldName extends Expression {

	private String name;

	public FieldName(Stack<Expression> stack, String name) {
		super(stack);
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

}
