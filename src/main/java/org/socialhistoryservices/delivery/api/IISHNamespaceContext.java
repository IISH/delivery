package org.socialhistoryservices.delivery.api;

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;

public class IISHNamespaceContext implements NamespaceContext {
    public String getNamespaceURI(String prefix) {
        switch (prefix) {
            case "marc":
                return "http://www.loc.gov/MARC21/slim";
            case "extraData":
            case "ns2":
                return "http://oclc.org/srw/extraData";
            case "iisg":
                return "http://www.iisg.nl/api/sru/";
            case "srw":
            case "ns1":
                return "http://www.loc.gov/zing/srw/";
            case "oai":
                return "http://www.openarchives.org/OAI/2.0/";
            case "ead":
                return "urn:isbn:1-931666-22-9";
        }
        return null;
    }

    public Iterator getPrefixes(String val) {
        return null;
    }

    public String getPrefix(String uri) {
        return null;
    }
}
