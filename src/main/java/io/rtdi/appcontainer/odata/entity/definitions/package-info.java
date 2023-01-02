@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
            @XmlNs(prefix="", namespaceURI="http://docs.oasis-open.org/odata/ns/edm")
    },
    namespace = "http://docs.oasis-open.org/odata/ns/edm"
)

package io.rtdi.appcontainer.odata.entity.definitions;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
