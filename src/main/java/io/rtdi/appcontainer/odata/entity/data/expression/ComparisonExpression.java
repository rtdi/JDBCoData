package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class ComparisonExpression extends Expression implements IBooleanExpression {

	private String operation;
	private Expression left;
	private Expression right;

	public ComparisonExpression(Stack<Expression> stack, String operation) {
		super(stack);
		this.operation = operation;
		left = stack.pop();
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		Expression e = ExpressionSet.getNextExpression(in, stack);
		if (e != null) {
			e.parse(in);
			right = e;
		}
	}

	@Override
	public String toString() {
		return "(" + left + " " + operation + " " + right + ")";
	}

	@Override
	public CharSequence getSQL() {
		return left.getSQL() + " " + getSQLOperation(operation) + " " + right.getSQL() + " ";
	}

	protected static CharSequence getSQLOperation(String op) {
		switch (op) {
		case "eq":
			return "=";
		case "ne":
			return "<>";
		case "gt":
			return ">";
		case "lt":
			return "<";
		case "ge":
			return ">=";
		case "le":
			return "<=";
		}
		return "?";
	}

}
