package io.rtdi.appcontainer.odata;

public class ODataException extends Exception {

	private static final long serialVersionUID = -2453054567440655983L;

	public ODataException(String message) {
		super(message);
	}

	public ODataException(String message, Exception e) {
		super(message, e);
	}

}
