package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public class NotExpression extends ExpressionSet implements IBooleanExpression {
	private IBooleanExpression right;

	public NotExpression(Stack<Expression> stack, ODataSchema table, List<IParameterValue> params) {
		super(stack, table, params);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		Expression e = ExpressionSet.getNextExpression(in, stack, table, getParams());
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
	public CharSequence getSQL() throws ODataException {
		return "not " + right.getSQL();
	}

}
