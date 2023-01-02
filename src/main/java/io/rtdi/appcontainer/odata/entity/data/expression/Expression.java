package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public abstract class Expression implements IExpression {
	protected Stack<Expression> stack;
	protected EntityType table;
	private List<IParameterValue> params;

	public Expression(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		this.stack = stack;
		this.table = table;
		this.params = params;
	}
	
	protected abstract void parse(CharBuffer in) throws ODataException;
	
	protected Expression addStack(Expression e) {
		stack.push(e);
		return e;
	}

	public List<IParameterValue> getParams() {
		return params;
	}
	
	public void addParam(IParameterValue o) {
		params.add(o);
	}
	
}
