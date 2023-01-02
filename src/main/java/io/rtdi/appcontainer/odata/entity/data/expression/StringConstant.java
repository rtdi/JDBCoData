package io.rtdi.appcontainer.odata.entity.data.expression;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Stack;

import io.rtdi.appcontainer.odata.ODataException;
import io.rtdi.appcontainer.odata.ODataTypes;
import io.rtdi.appcontainer.odata.entity.definitions.EntityType;
import io.rtdi.appcontainer.odata.entity.definitions.EntityTypeProperty;

public class StringConstant extends Expression implements IParameterValue {

	private StringBuilder text;
	private EntityTypeProperty datatype;

	public StringConstant(Stack<Expression> stack, EntityType table, List<IParameterValue> params) {
		super(stack, table, params);
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
					addParam(this);
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
	public CharSequence getSQL() throws ODataException {
		if (datatype == null) {
			return "\'" + text + "\' ";
		} else {
			return "? ";
		}
	}

	public void setDataType(EntityTypeProperty datatype) {
		this.datatype = datatype;
	}
	
	@Override
	public Object getValue() throws ODataException {
		return ODataTypes.convertToJDBC(text.toString(), datatype);
	}

}
