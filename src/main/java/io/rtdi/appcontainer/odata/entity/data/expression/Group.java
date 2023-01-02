package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public class Group extends ExpressionSet implements IBooleanExpression {
	private IBooleanExpression group;

	public Group(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		while (in.hasRemaining() && in.charAt(0) != ')') {
			super.parse(in);
		}
		Expression o = stack.pop();
		if (o instanceof IBooleanExpression) {
			group = (IBooleanExpression) o;
		} else {
			throw new ODataException("This is not a valid group clause");
		}
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
