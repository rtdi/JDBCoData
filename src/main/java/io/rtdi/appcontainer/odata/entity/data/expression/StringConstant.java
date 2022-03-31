package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;

public class StringConstant extends Expression {

	private StringBuilder text;

	public StringConstant(Stack<Expression> stack) {
		super(stack);
		text = new StringBuilder();
	}

	@Override
	protected void parse(CharBuffer in) throws ODataException {
		boolean escaped = false;
		while (in.hasRemaining()) {
			char c = in.get();
			if (escaped) {
				escaped = false;
				text.append(c);
			} else {
				switch (c) {
				case '\\':
					escaped = true;
				case '\'': {
					return;
				}
				default:
					text.append(c);
					break;
				}
			}
		}
	}

	@Override
	public String toString() {
		return text.toString();
	}

	@Override
	public CharSequence getSQL() {
		return "\'" + text + "\' ";
	}

}
