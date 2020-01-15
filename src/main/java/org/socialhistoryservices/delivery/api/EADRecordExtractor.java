package org.socialhistoryservices.delivery.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.*;

public class EADRecordExtractor implements IISHRecordExtractor {
    private static final Log logger = LogFactory.getLog(EADRecordExtractor.class);

    private XPath xpath;
    private XPathExpression xpTitle, xpAuthor, xpPhysicalDescription, xpUnitId,
            xpAccessAndUse, xpAccessRestrict, xpP, xpParent, xpContainer;

    public EADRecordExtractor() {
        XPathFactory factory = XPathFactory.newInstance();
        xpath = factory.newXPath();
        xpath.setNamespaceContext(new IISHNamespaceContext());

        try {
            xpContainer = xpath.compile(".//ead:container");
            xpTitle = xpath.compile("normalize-space(.//ead:unittitle)");
            xpAuthor = xpath.compile("normalize-space(.//ead:origination[@label='Creator']/ead:persname)");
            xpPhysicalDescription = xpath.compile(
                    "normalize-space(.//ead:physdesc[@label='Physical Description']/ead:extent)");
            xpUnitId = xpath.compile(".//ead:unitid");
            xpAccessAndUse = xpath.compile(".//ead:descgrp[@type='access_and_use']");
            xpAccessRestrict = xpath.compile(".//ead:accessrestrict");
            xpP = xpath.compile("normalize-space(./ead:p[1])");
            xpParent = xpath.compile("(" +
                    "./ancestor::ead:c01|" +
                    "./ancestor::ead:c02|" +
                    "./ancestor::ead:c03|" +
                    "./ancestor::ead:c04|" +
                    "./ancestor::ead:c05|" +
                    "./ancestor::ead:c06|" +
                    "./ancestor::ead:c07|" +
                    "./ancestor::ead:c08|" +
                    "./ancestor::ead:c09|" +
                    "./ancestor::ead:c10|" +
                    "./ancestor::ead:c11|" +
                    "./ancestor::ead:c12)[last()]");
        }
        catch (XPathExpressionException ex) {
            logger.error("Failed initializing XPath expressions");
        }
    }

    /**
     * Parses a node to metadata of a record.
     *
     * @param node The node to parse.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    @Override
    public ExternalRecordInfo getRecordMetadata(Node node) throws NoSuchPidException {
        return getRecordMetadata(node, null);
    }

    /**
     * Parses a node to metadata of a record.
     *
     * @param node The node to parse.
     * @param item The item.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    @Override
    public ExternalRecordInfo getRecordMetadata(Node node, String item) throws NoSuchPidException {
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();

        String author = XmlUtils.evaluate(xpAuthor, node);
        if (author != null && !author.isEmpty()) {
            author = author.trim();
            externalInfo.setAuthor(stripToSize(author, 125));
        }

        String title = XmlUtils.evaluate(xpTitle, node);
        if (title != null && !title.isEmpty()) {
            // Strip trailing slashes
            title = title.trim();
            // Some titles from the API SRW exceed 255 characters.
            // Strip them to 251 characters (up to 252 , exclusive) to save up for the dash and \0 termination.
            // EDIT: Trim this to ~125 characters for readability (this is the current max size of the field).
            title = stripToSize(title, 125);
            externalInfo.setTitle(title);
        } else {
            externalInfo.setTitle("Unknown Record");
        }

        externalInfo.setMaterialType(ExternalRecordInfo.MaterialType.ARCHIVE);
        externalInfo.setPublicationStatus(ExternalRecordInfo.PublicationStatus.UNKNOWN);
        externalInfo.setRestriction(evaluateRestriction(node, findItemNode(node, item)));
        externalInfo.setContainer(getContainer(node, findItemNode(node, item)));

        String physicalDescription = XmlUtils.evaluate(xpPhysicalDescription, node);
        externalInfo.setPhysicalDescription((physicalDescription != null) ? physicalDescription.trim() : null);

        return externalInfo;
    }

    /**
     * Get a map of holding signatures associated with this node,
     * linking to additional holding info provided by the API.
     *
     * @param node The node to parse.
     * @return A map of found (signature,holding info) tuples, or an empty map if none were found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadata(Node node) throws NoSuchPidException {
        return getHoldingMetadata(node, null);
    }

    /**
     * Get a map of holding signatures associated with this node,
     * linking to additional holding info provided by the API.
     *
     * @param node The node to parse.
     * @param item The item.
     * @return A map of found (signature,holding info) tuples, or an empty map if none were found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadata(Node node, String item) throws NoSuchPidException {
        Map<String, ExternalHoldingInfo> retMap = new HashMap<>();

        Node itemNode = (item != null) ? findItemNode(node, item) : node;
        if (itemNode == null) {
            throw new NoSuchPidException();
        }

        try {
            String barcode = xpUnitId.evaluate(node);
            if (item != null) {
                barcode += "." + item;
            }

            ExternalHoldingInfo eh = new ExternalHoldingInfo();
            eh.setBarcode(barcode);

            retMap.put((item != null) ? item : barcode, eh);
        }
        catch (XPathExpressionException ignored) {
            logger.debug("getHoldingMetadata(): Invalid XPath", ignored);
        }

        return retMap;
    }

    @Override
    public String getUnitIdsFromContainer(Node node, String pid, String container, boolean includeCurrentContainer) throws NoSuchPidException {
        ArrayList<String> listOfContainers = new ArrayList<String>();
        String[] parentPidAndItem = Utils.getParentPidAndItem(pid);

        try {
            NodeList itemNodes = (NodeList) xpath.evaluate(".//ead:dsc//ead:unitid", node, XPathConstants.NODESET);

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node child = itemNodes.item(i);

                if (child.getTextContent().toLowerCase().startsWith(container.toLowerCase() + " ")) { // don't remove space " "
                    if (includeCurrentContainer || !(child.getTextContent().toLowerCase().equals(parentPidAndItem[1].toLowerCase()))) {
                        listOfContainers.add(parentPidAndItem[0] + "." + child.getTextContent());
                    }
                }
            }
        }
        catch (XPathExpressionException ex) {
            return null;
        }

        return String.join(",", listOfContainers);
    }

    private String stripToSize(String string, int size) {
        if (string.length() > size) {
            string = string.substring(0, size);
            string = string.trim();
        }
        return string;
    }

    private Node findItemNode(Node node, String item) {
        try {
            if (item == null) {
                return null;
            }

            Node itemNode = (Node) xpath.evaluate(".//ead:dsc//ead:unitid[text()='" + item + "']",
                    node, XPathConstants.NODE);
            Node parentNode = (Node) xpParent.evaluate(itemNode, XPathConstants.NODE);

            // An item node is only valid if is a leaf item node (has no children with items)
            if (((NodeList) xpUnitId.evaluate(parentNode, XPathConstants.NODESET)).getLength() == 1) {
                return parentNode;
            }
            return null;
        }
        catch (XPathExpressionException ex) {
            return null;
        }
    }

    private ExternalRecordInfo.Restriction evaluateRestriction(Node node, Node itemNode) {
        try {
            Element accessAndUse = (Element) xpAccessAndUse.evaluate(node, XPathConstants.NODE);
            Element accessRestrict = (Element) xpAccessRestrict.evaluate(accessAndUse, XPathConstants.NODE);
            String restriction = xpP.evaluate(accessRestrict);

            String type = accessRestrict.getAttribute("type").toLowerCase();
            if (type.equals("date")) {
                restriction = "date";
            }

            if (type.equals("part") && (itemNode != null)) {
                accessRestrict = (Element) xpAccessRestrict.evaluate(itemNode, XPathConstants.NODE);
                if (accessRestrict != null) {
                    restriction = accessRestrict.getAttribute("type");
                } else {
                    restriction = "open";
                }
            }

            switch (restriction.trim().toLowerCase()) {
                case "gesloten":
                case "closed":
                    return ExternalRecordInfo.Restriction.CLOSED;
                case "beperkt":
                case "restricted":
                    return ExternalRecordInfo.Restriction.RESTRICTED;
                case "date":
                    return ExternalRecordInfo.Restriction.DATE_RESTRICTED;
                default:
                    return ExternalRecordInfo.Restriction.OPEN;
            }
        }
        catch (XPathExpressionException e) {
            return ExternalRecordInfo.Restriction.OPEN;
        }
    }

    private String getContainer(Node node, Node itemNode) {
        try {
            Element elContainer = (Element) xpContainer.evaluate(itemNode, XPathConstants.NODE);
            return elContainer.getTextContent().trim();
        }
        catch (NullPointerException | XPathExpressionException ex) {
            return null;
        }
    }
}
