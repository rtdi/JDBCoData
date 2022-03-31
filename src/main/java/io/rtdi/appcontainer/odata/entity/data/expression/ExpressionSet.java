package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public abstract class ExpressionSet extends Expression {

	public ExpressionSet(Stack<Expression> stack) {
		super(stack);
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
		Expression e = getNextExpression(in, stack);
		if (e != null) {
			addStack(e).parse(in);
		}
	}
	
	protected static Expression getNextExpression(CharBuffer in, Stack<Expression> stack) throws ODataException {
		StringBuilder lastword = new StringBuilder();
		while (in.hasRemaining()) {
			char c = in.get();
			switch (c) {
			case '\'':
				return new StringConstant(stack);
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
				return new NumberConstant(stack, c);
			case '(':
				return new Group(stack);
			case ' ': 
				if (lastword.toString().trim().length() != 0) {
					return parseWord(lastword, in, stack);
				}
				break;
			default:
				lastword.append(c);
				break;
			}
		}
		return null;
	}
	
	private static Expression parseWord(CharSequence word, CharBuffer in, Stack<Expression> stack) throws ODataException {
		switch (word.toString()) {
		case "not":
			return new NotExpression(stack);
		case "eq":
		case "ne":
		case "gt":
		case "lt":
		case "ge":
		case "le":
			return new ComparisonExpression(stack, word.toString());
		case "and":
		case "or":
			return new BinaryExpression(stack, word.toString());
		default:
			return new FieldName(stack, word.toString());
		}
	}

}
