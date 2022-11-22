package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public class Group extends ExpressionSet implements IBooleanExpression {
	private IBooleanExpression group;

	public Group(Stack<Expression> stack, ODataSchema table, List<Object> params) {
		super(stack, table, params);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		while (in.hasRemaining() && in.get(in.position()) != ')') {
			super.parse(in);
		}
		group = (IBooleanExpression) stack.pop();
	}
	
	@Override
	public String toString() {
		return "(" + group + ")";
	}

	@Override
	public CharSequence getSQL() throws ODataException {
		return "(" + group.getSQL() + ") ";
	}
}
