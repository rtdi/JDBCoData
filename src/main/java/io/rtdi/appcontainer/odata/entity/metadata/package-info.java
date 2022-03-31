@XmlSchema(
    elementFormDefault = XmlNsForm.QUALIFIED,
    xmlns = {
            @XmlNs(prefix="edmx", namespaceURI="http://docs.oasis-open.org/odata/ns/edmx"),
            @XmlNs(prefix="", namespaceURI="http://docs.oasis-open.org/odata/ns/edm")
    },
    namespace = "http://docs.oasis-open.org/odata/ns/edm"
)

package io.rtdi.appcontainer.odata.entity.metadata;

import jakarta.xml.bind.annotation.*;
