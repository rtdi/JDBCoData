package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class Filter extends ExpressionSet implements IBooleanExpression {
	private Expression expression;

	/*
	 * http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#_Toc31360957
	 */
	
	public Filter() throws ODataException {
		super(new Stack<>());
	}

	@Override
	public void parse(CharBuffer in) throws ODataException {
		while (in.hasRemaining()) {
			super.parse(in);
		}
		this.expression = stack.pop();
	}

	public Expression getExpression() {
		return expression;
	}

	@Override
	public CharSequence getSQL() {
		return expression.getSQL();
	}
	
}
