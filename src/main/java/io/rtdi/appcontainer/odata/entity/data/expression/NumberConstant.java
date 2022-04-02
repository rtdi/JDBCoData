package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.entity.metadata.ODataSchema;

public class NumberConstant extends Expression {

	private StringBuilder text;

	public NumberConstant(Stack<Expression> stack, char c, ODataSchema table, List<Object> params) {
		super(stack, table, params);
		text = new StringBuilder(c);
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		while (in.hasRemaining()) {
			char c = in.get();
			switch (c) {
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
			case '.':
				text.append(c);
				break;
			case ' ':
				addParam(text.toString());
				return;
			case ')':
				in.position(in.position()-1);
				addParam(text.toString());
				return;
			default:
				throw new ODataException("Not a valid number \"" + text.toString() + c + "...\"");
			}
		}
		addParam(text.toString()); // in case this was the last char of the expression
	}

	@Override
	public String toString() {
		return text + " ";
	}

	@Override
	public CharSequence getSQL() {
		return "? ";
	}

}
