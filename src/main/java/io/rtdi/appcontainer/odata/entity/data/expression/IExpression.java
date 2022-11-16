package io.rtdi.appcontainer.odata.entity.data.expression;

import io.rtdi.appcontainer.odata.ODataException;

public interface IExpression {

	CharSequence getSQL() throws ODataException;
}
