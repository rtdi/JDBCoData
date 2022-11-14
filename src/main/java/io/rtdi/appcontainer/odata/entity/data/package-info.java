@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
            @XmlNs(prefix="a", namespaceURI="http://www.w3.org/2005/Atom"),
            @XmlNs(prefix="m", namespaceURI="http://docs.oasis-open.org/odata/ns/metadata"),
            @XmlNs(prefix="d", namespaceURI="http://docs.oasis-open.org/odata/ns/data")
    },
    namespace = "http://www.w3.org/2005/Atom"
)

package io.rtdi.appcontainer.odata.entity.data;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
