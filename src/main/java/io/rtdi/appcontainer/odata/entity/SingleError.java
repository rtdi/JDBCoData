package io.rtdi.appcontainer.odata.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;

public class SingleError {

	private String code;
	private String message;
	private InnerError innererror;
		
	public SingleError(Exception e) {
		message = e.getMessage();
		innererror = new InnerError(e);
	}

	@XmlElement
	public String getCode() {
		return code;
	}

	@XmlElement
	public String getMessage() {
		return message;
	}
	
	/*
 	"innererror": {
	      "trace": [...],
	      "context": {...}
	}
	 */
	@XmlElement
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
		
		@XmlElement
		public List<String> getTrace() {
			return trace;
		}
	}
	
}