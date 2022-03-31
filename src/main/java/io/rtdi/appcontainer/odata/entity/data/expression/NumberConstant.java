package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class NumberConstant extends Expression {

	private StringBuilder text;

	public NumberConstant(Stack<Expression> stack, char c) {
		super(stack);
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
				return;
			case ')':
				in.position(in.position()-1);
				return;
			default:
				throw new ODataException("Not a valid number \"" + text.toString() + c + "...\"");
			}
		}
	}

	@Override
	public String toString() {
		return text.toString();
	}

	@Override
	public CharSequence getSQL() {
		return text + " ";
	}

}
