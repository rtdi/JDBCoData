package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public abstract class Expression implements IExpression {
	protected Stack<Expression> stack;

	public Expression(Stack<Expression> stack) {
		this.stack = stack;
	}
	
	protected abstract void parse(CharBuffer in) throws ODataException;
	
	protected Expression addStack(Expression e) {
		stack.push(e);
		return e;
	}
	
}
