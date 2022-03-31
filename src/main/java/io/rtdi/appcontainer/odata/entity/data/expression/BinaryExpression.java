package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class BinaryExpression extends Expression {

	private String operation;
	private IBooleanExpression left;
	private IBooleanExpression right;

	public BinaryExpression(Stack<Expression> stack, String operation) {
		super(stack);
		this.operation = operation;
		left = (IBooleanExpression) stack.pop();
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		do {
			Expression e = ExpressionSet.getNextExpression(in, stack);
			if (e != null) {
				addStack(e).parse(in);
			}
		} while (in.hasRemaining() && !(stack.peek() instanceof IBooleanExpression));
		Expression e = stack.pop();
		if (e instanceof IBooleanExpression) {
			right = (IBooleanExpression) e;
		}
	}

	@Override
	public String toString() {
		return "(" + left + " " + operation + " " + right + ")";
	}

	@Override
	public CharSequence getSQL() {
		return left.getSQL() + " " + getSQLOperation(operation) + " " + right.getSQL();
	}

	protected static CharSequence getSQLOperation(String op) {
		switch (op) {
		case "and":
			return "and";
		case "or":
			return "or";
		}
		return "?";
	}

}
