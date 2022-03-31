package io.rtdi.appcontainer.odata.entity;

import java.util.ArrayList;
import java.util.List;

public class SingleError {

	private String code;
	private String message;
	private InnerError innererror;
		
	public SingleError(Exception e) {
		message = e.getMessage();
		innererror = new InnerError(e);
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	/*
 	"innererror": {
	      "trace": [...],
	      "context": {...}
	}
	 */
	public InnerError getInnererror() {
		return innererror;
	}
	
	public static class InnerError {
		private List<String> trace;

		public InnerError(Exception e) {
			StackTraceElement[] t = e.getStackTrace();
			if (t != null) {
				trace = new ArrayList<>();
				for (StackTraceElement item : t) {
					trace.add(item.toString());
				}
			}
		}
		
		public List<String> getTrace() {
			return trace;
		}
	}
	
}