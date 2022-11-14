package io.rtdi.appcontainer.odata.entity;

import java.sql.SQLException;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ODataError {

	private SingleError error;
	private int statuscode = 501;
	
	public ODataError() {
		super();
	};
	
	public ODataError(Exception e) {
		error = new SingleError(e);
		if (e instanceof SQLException) {
			this.statuscode = 416;
		}
	}
	/*
		{
		  "error": {
		    "code": "err123",
		    "message": "Unsupported functionality",
		    "target": "query",
		    "details": [
		      {
		       "code": "forty-two",
		       "target": "$search", 
		       "message": "$search query option not supported"
		      }
		    ],
		    "innererror": {
		      "trace": [...],
		      "context": {...}
		    }
		  }
		}
	 */
	
	@XmlElement(name = "Error")
	public SingleError getError() {
		return error;
	}

	public int getStatusCode() {
		return statuscode;
	}
}