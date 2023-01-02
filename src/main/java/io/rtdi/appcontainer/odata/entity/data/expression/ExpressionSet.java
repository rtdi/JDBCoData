package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;

public abstract class ExpressionSet extends Expression {

	public ExpressionSet(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
	}

	/* private List<ExpressionSet> parts;

	public ExpressionSet add(ExpressionSet child) {
		if (parts != null) {
			parts = new ArrayList<>();
		}
		parts.add(child);
		return child;
	} */

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		Expression e = getNextExpression(in, stack, table, getParams());
		if (e != null) {
			addStack(e).parse(in);
		}
	}
	
	protected static Expression getNextExpression(CharBuffer in, Stack<Expression> stack, EntityType table, List<IParameterValue> params) throws ODataException {
		StringBuilder lastword = new StringBuilder();
		while (in.hasRemaining()) {
			char c = in.get();
			switch (c) {
			case '\'':
				return new StringConstant(stack, table, params);
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '+':
			case '-':
				in.position(in.position()-1); // rewind by 1 so that NumberConstant can read the first digit
				return new NumberConstant(stack, c, table, params);
			case '(':
				if (lastword != null && lastword.length() == 0) {
					return new Group(stack, table, params);
				} else {
					// This is a function like "contains(...)"
					return getFunction(lastword.toString(), stack, table, params);
				}
			case ' ': 
				if (lastword.toString().trim().length() != 0) {
					return parseWord(lastword, in, stack, table, params);
				}
				break;
			default:
				lastword.append(c);
				break;
			}
		}
		return null;
	}
	
	private static Expression getFunction(String functionname, Stack<Expression> stack, EntityType table, List<IParameterValue> params) throws ODataException {
		switch (functionname) {
		case "contains":
			return new LikeExpression(stack, table, params);
		}
		throw new ODataException(String.format("The term %s is not a supported function", functionname));
	}

	private static Expression parseWord(CharSequence word, CharBuffer in, Stack<Expression> stack, EntityType table, List<IParameterValue> params) throws ODataException {
		switch (word.toString()) {
		case "null":
			return new NullConstant(stack, table, params);
		case "not":
			return new NotExpression(stack, table, params);
		case "eq":
		case "ne":
		case "gt":
		case "lt":
		case "ge":
		case "le":
			return new ComparisonExpression(stack, word.toString(), table, params);
		case "and":
		case "or":
			return new BinaryExpression(stack, word.toString(), table, params);
		default:
			return new FieldName(stack, word.toString(), table, params);
		}
	}

}
