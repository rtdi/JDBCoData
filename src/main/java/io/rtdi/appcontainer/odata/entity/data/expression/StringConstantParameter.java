package io.rtdi.appcontainer.odata.entity.data.expression;

import io.rtdi.appcontainer.odata.ODataException;

public class StringConstantParameter implements IParameterValue {

	private String value;

	public StringConstantParameter(String value) {
		this.value = value;
	}

	@Override
	public Object getValue() throws ODataException {
		return value;
	}

}
