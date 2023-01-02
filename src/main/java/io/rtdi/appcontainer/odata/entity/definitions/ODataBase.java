package io.rtdi.appcontainer.odata.entity.definitions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.rtdi.appcontainer.odata.ODataUtils;
import jakarta.xml.bind.annotation.XmlElement;

public class ODataBase {
	private Map<String, Annotation> annotations;
	public void setComment(String comment) {
		addAnnotation(ODataUtils.JDBC_COMMENT, comment);
	}
	
	public void addAnnotation(String term, String value) {
		if (annotations == null) {
			annotations = new HashMap<>();
		}
		annotations.put(term, new Annotation(term, value));
	}

	@XmlElement(name = "Annotation")
	public Collection<Annotation> getAnnotations() {
		if (annotations == null) {
			return null;
		} else {
			return annotations.values();
		}
	}

}
