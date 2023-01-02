package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public class LikeExpression extends ExpressionSet implements IBooleanExpression {
	private String[] pair = new String[2];

	public LikeExpression(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		StringBuilder b = new StringBuilder();
		while (in.hasRemaining()) {
			char c = in.get();
			if (c == ')') {
				break;
			} else if (c == ',') {
				pair[0] = b.toString().trim();
				b = new StringBuilder();
			} else {
				b.append(c);
			}
		}
		pair[1] = b.toString().trim();
		if (pair[1].startsWith("'")) {
			pair[1] = pair[1].substring(1, pair[1].length()-1);
		}
		pair[1] = '%' + pair[1] + '%';
		addParam(new StringConstantParameter(pair[1]));
	}
	
	@Override
	public String toString() {
		return pair[0] + " like '" + pair[1] + "'";
	}

	@Override
	public CharSequence getSQL() throws ODataException {
		return pair[0] + " like ?";
	}

}
