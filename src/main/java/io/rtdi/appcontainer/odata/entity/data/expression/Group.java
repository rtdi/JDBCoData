package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class Group extends ExpressionSet implements IBooleanExpression {
	private IBooleanExpression group;

	public Group(Stack<Expression> stack) {
		super(stack);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		while (in.hasRemaining() && in.charAt(in.position()) != ')') {
			super.parse(in);
		}
		group = (IBooleanExpression) stack.pop();
	}
	
	@Override
	public String toString() {
		return "(" + group + ")";
	}

	@Override
	public CharSequence getSQL() {
		return "(" + group.getSQL() + ") ";
	}

}
