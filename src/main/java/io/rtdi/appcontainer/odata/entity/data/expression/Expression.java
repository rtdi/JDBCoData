package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public abstract class Expression implements IExpression {
	protected Stack<Expression> stack;
	protected ODataSchema table;
	private List<Object> params;

	public Expression(Stack<Expression> stack, ODataSchema table, List<Object> params) {
		this.stack = stack;
		this.table = table;
		this.params = params;
	}
	
	protected abstract void parse(CharBuffer in) throws ODataException;
	
	protected Expression addStack(Expression e) {
		stack.push(e);
		return e;
	}

	public List<Object> getParams() {
		return params;
	}
	
	public void addParam(Object o) {
		params.add(o);
	}
}
