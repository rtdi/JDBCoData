package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public class ComparisonExpression extends Expression implements IBooleanExpression {

	private String operation;
	private Expression left;
	private Expression right;

	public ComparisonExpression(Stack<Expression> stack, String operation, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
		this.operation = operation;
		left = stack.pop();
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		Expression e = ExpressionSet.getNextExpression(in, stack, table, getParams());
		if (e != null) {
			e.parse(in);
			right = e;
		}
		if (left instanceof FieldName && right instanceof StringConstant) {
			((StringConstant) right).setDataType(((FieldName) left).getDataType());
		}
		if (right instanceof FieldName && left instanceof StringConstant) {
			((StringConstant) left).setDataType(((FieldName) right).getDataType());
		}
	}

	@Override
	public String toString() {
		return "(" + left + " " + operation + " " + right + ")";
	}

	@Override
	public CharSequence getSQL() throws ODataException {
		if (left instanceof NullConstant) {
			return right.getSQL() + " " + getNullTest();
		} else if (right instanceof NullConstant) {
			return left.getSQL() + " " + getNullTest();
		} else {
			return left.getSQL() + " " + getSQLOperation() + " " + right.getSQL() + " ";
		}
	}

	protected CharSequence getNullTest() {
		switch (operation) {
		case "eq":
			return " is null";
		case "ne":
			return " is not null";
		case "gt":
			return "> null";
		case "lt":
			return "< null";
		case "ge":
			return ">= null";
		case "le":
			return "<= null";
		}
		return "?";
	}

	protected CharSequence getSQLOperation() {
		switch (operation) {
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
