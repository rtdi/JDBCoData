package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class NotExpression extends ExpressionSet implements IBooleanExpression {
	private IBooleanExpression right;

	public NotExpression(Stack<Expression> stack) {
		super(stack);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		Expression e = ExpressionSet.getNextExpression(in, stack);
		if (e != null) {
			e.parse(in);
			right = (IBooleanExpression) e;
		}
	}

	@Override
	public String toString() {
		return "not " + right;
	}

	@Override
	public CharSequence getSQL() {
		return "not " + right.getSQL();
	}

}
