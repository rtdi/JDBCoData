@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
            @XmlNs(prefix="edmx", namespaceURI="http://docs.oasis-open.org/odata/ns/edmx")
    },
    namespace = "http://docs.oasis-open.org/odata/ns/edmx"
)

package io.rtdi.appcontainer.odata.entity.metadata;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;
