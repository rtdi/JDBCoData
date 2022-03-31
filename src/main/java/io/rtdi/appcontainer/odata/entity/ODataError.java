package io.rtdi.appcontainer.odata.entity;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ODataError {

	private SingleError error;
	
	public ODataError(Exception e) {
		error = new SingleError(e);
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
}