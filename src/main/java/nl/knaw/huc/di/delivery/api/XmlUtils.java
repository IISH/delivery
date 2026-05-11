package nl.knaw.huc.di.delivery.api;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

public class XmlUtils {
    public static XPathExpression getXPathForMarc(XPath xpath, String tag, char code) throws XPathExpressionException {
        return getXPathForMarc(xpath, tag, code, "");
    }

    public static XPathExpression getXPathForMarc(XPath xpath, String tag, char code, String searchPath)
        throws XPathExpressionException {
        return xpath.compile(
            searchPath + "marc:datafield[@tag=" + tag + "]/marc:subfield[@code=\"" + code + "\"]");
    }

    public static XPathExpression getXPathForMarcTag(XPath xpath, String tag) throws XPathExpressionException {
        return xpath.compile("marc:datafield[@tag=" + tag + "]");
    }

    public static XPathExpression getXPathForMarcSubfield(XPath xpath, char code) throws XPathExpressionException {
        return xpath.compile("marc:subfield[@code=\"" + code + "\"]");
    }

    public static String evaluate(XPathExpression expression, Node node) {
        try {
            String value = expression.evaluate(node);
            return value.isEmpty() ? null : value;
        }
        catch (XPathExpressionException ex) {
            return null;
        }
    }
}
