package org.socialhistoryservices.delivery.api;

import java.util.*;
import javax.xml.xpath.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.socialhistoryservices.delivery.record.util.Inventory;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;

public class EADMetadataRecordExtractor implements MetadataRecordExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EADMetadataRecordExtractor.class);

    private static final XPath xpath;
    private static final XPathExpression xpTitle, xpTitleItem, xpAuthor, xpPhysicalDescription, xpUnitId, xpUnitIdItem,
            xpContainer, xpInventory, xpAccessAndUse, xpAccessRestrict, xpP, xpParent, xpChildren,
            xpArchive931, xpArchiveLocation, xpArchiveMeter, xpArchiveNumbers, xpArchiveFormat, xpArchiveNote;

    private final String parentPid;
    private final String item;
    private final String itemSep;

    private final Node ead;
    private final Node archival;

    static {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            xpath.setNamespaceContext(new IISHNamespaceContext());

            xpTitle = xpath.compile("normalize-space(.//ead:unittitle)");
            xpTitleItem = xpath.compile("normalize-space(./ead:did/ead:unittitle)");
            xpAuthor = xpath.compile("normalize-space(.//ead:origination[@label='Creator']/ead:persname)");
            xpPhysicalDescription = xpath.compile(
                    "normalize-space(.//ead:physdesc[@label='Physical Description']/ead:extent)");
            xpUnitId = xpath.compile(".//ead:unitid");
            xpUnitIdItem = xpath.compile("./ead:did/ead:unitid");
            xpContainer = xpath.compile("normalize-space(.//ead:container[@type='box'])");
            xpInventory = xpath.compile(".//ead:dsc[@type='combined']");
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
            xpChildren = xpath.compile("(" +
                    "./ead:c01|" +
                    "./ead:c02|" +
                    "./ead:c03|" +
                    "./ead:c04|" +
                    "./ead:c05|" +
                    "./ead:c06|" +
                    "./ead:c07|" +
                    "./ead:c08|" +
                    "./ead:c09|" +
                    "./ead:c10|" +
                    "./ead:c11|" +
                    "./ead:c12)");

            xpArchive931 = XmlUtils.getXPathForMarcTag(xpath, "931");
            xpArchiveLocation = XmlUtils.getXPathForMarcSubfield(xpath, 'a');
            xpArchiveMeter = XmlUtils.getXPathForMarcSubfield(xpath, 'b');
            xpArchiveNumbers = XmlUtils.getXPathForMarcSubfield(xpath, 'c');
            xpArchiveFormat = XmlUtils.getXPathForMarcSubfield(xpath, 'e');
            xpArchiveNote = XmlUtils.getXPathForMarcSubfield(xpath, 'f');
        }
        catch (XPathExpressionException ex) {
            throw new RuntimeException(ex);
        }
    }

    public EADMetadataRecordExtractor(String parentPid, String item, String itemSep, Node ead, Node archival) {
        this.parentPid = parentPid;
        this.item = item;
        this.itemSep = itemSep;

        this.ead = ead;
        this.archival = archival;
    }

    /**
     * Returns the PID of the record.
     *
     * @return The PID.
     */
    @Override
    public String getPid() {
        return (item != null) ? parentPid + itemSep + item : parentPid;
    }

    /**
     * Extracts the metadata of the record.
     *
     * @return The metadata of the record, if found.
     */
    @Override
    public ExternalRecordInfo getRecordMetadata() {
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();

        String author = XmlUtils.evaluate(xpAuthor, ead);
        if (author != null && !author.isEmpty()) {
            author = author.trim();
            externalInfo.setAuthor(MetadataRecordExtractor.stripToSize(author, 125));
        }

        String title = XmlUtils.evaluate(xpTitle, ead);
        if (title != null && !title.isEmpty()) {
            // Strip trailing slashes
            title = title.trim();
            // Some titles from the API SRW exceed 255 characters.
            // Strip them to 251 characters (up to 252 , exclusive) to save up for the dash and \0 termination.
            // EDIT: Trim this to ~125 characters for readability (this is the current max size of the field).
            title = MetadataRecordExtractor.stripToSize(title, 125);
            externalInfo.setTitle(title);
        }
        else {
            externalInfo.setTitle("Unknown Record");
        }

        externalInfo.setMaterialType(ExternalRecordInfo.MaterialType.ARCHIVE);
        externalInfo.setPublicationStatus(ExternalRecordInfo.PublicationStatus.UNKNOWN);
        externalInfo.setRestriction(evaluateRestriction());

        String container = (item != null) ? XmlUtils.evaluate(xpContainer, findItemNode()) : null;
        externalInfo.setContainer(container);

        String physicalDescription = XmlUtils.evaluate(xpPhysicalDescription, ead);
        externalInfo.setPhysicalDescription((physicalDescription != null) ? physicalDescription.trim() : null);

        Inventory inventory = getInventory();
        externalInfo.setInventory(inventory);

        return externalInfo;
    }

    /**
     * Get a map of holding signatures associated with this records,
     * linking to additional holding info provided by the API.
     *
     * @return A map of found (signature, holding info) tuples, or an empty map if none were found.
     */
    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadata() {
        Map<String, ExternalHoldingInfo> retMap = new HashMap<>();

        Node itemNode = (item != null) ? findItemNode() : ead;
        if (itemNode == null)
            return retMap;

        try {
            String barcode = xpUnitId.evaluate(ead);
            if (item != null)
                barcode = barcode.trim() + "." + item;

            ExternalHoldingInfo eh = new ExternalHoldingInfo();
            eh.setBarcode(barcode);

            retMap.put((item != null) ? item : barcode, eh);
        }
        catch (XPathExpressionException ignored) {
            LOGGER.debug("getHoldingMetadata(): Invalid XPath", ignored);
        }

        return retMap;
    }

    /**
     * Obtains archive holding info of a record.
     *
     * @return A list with the archive metadata of the record, if found.
     */
    @Override
    public List<ArchiveHoldingInfo> getArchiveHoldingInfo() {
        List<ArchiveHoldingInfo> info = new ArrayList<>();

        // Child records should look for archive holding info at their parent
        if (item == null && archival != null) {
            try {
                NodeList archiveList = (NodeList) xpArchive931.evaluate(archival, XPathConstants.NODESET);
                if (archiveList != null) {
                    for (int i = 0; i < archiveList.getLength(); i++) {
                        Node archiveItem = archiveList.item(i);

                        ArchiveHoldingInfo ahi = new ArchiveHoldingInfo();
                        ahi.setShelvingLocation(XmlUtils.evaluate(xpArchiveLocation, archiveItem));
                        ahi.setMeter(XmlUtils.evaluate(xpArchiveMeter, archiveItem));
                        ahi.setNumbers(XmlUtils.evaluate(xpArchiveNumbers, archiveItem));
                        ahi.setFormat(XmlUtils.evaluate(xpArchiveFormat, archiveItem));
                        ahi.setNote(XmlUtils.evaluate(xpArchiveNote, archiveItem));

                        if (ahi.getShelvingLocation() != null || ahi.getMeter() != null ||
                                ahi.getNumbers() != null || ahi.getFormat() != null || ahi.getNote() != null) {
                            info.add(ahi);
                        }
                    }
                }
            }
            catch (XPathExpressionException ignored) {
                LOGGER.debug("getArchiveHoldingInfo(): Invalid XPath", ignored);
            }
        }

        return info;
    }

    /**
     * Obtains metadata record extractors for all container siblings of the current record.
     * These records do not only share the same parent record, but also share a common container.
     *
     * @return A set of metadata record extractors for the container siblings.
     */
    public Set<MetadataRecordExtractor> getRecordExtractorsForContainerSiblings() {
        Set<MetadataRecordExtractor> recordExtractors = new HashSet<>();

        Node itemNode = findItemNode();
        String container = itemNode != null ? XmlUtils.evaluate(xpContainer, itemNode) : null;

        if (itemNode == null || container == null)
            return recordExtractors;

        try {
            NodeList containerNodes = (NodeList) xpath.evaluate(
                    ".//ead:dsc//ead:container[@type='box'][normalize-space(text())='" + container + "']",
                    ead, XPathConstants.NODESET);

            for (int i = 0; i < containerNodes.getLength(); i++) {
                Node parentItemsNode = (Node) xpParent.evaluate(containerNodes.item(i), XPathConstants.NODE);
                NodeList itemNodes = (NodeList) xpUnitId.evaluate(parentItemsNode, XPathConstants.NODESET);

                // An item node is only valid if is a leaf item node (has no children with items)
                if (itemNodes.getLength() == 1) {
                    String siblingItem = itemNodes.item(0).getTextContent().trim();
                    if (!item.equals(siblingItem)) {
                        EADMetadataRecordExtractor recordExtractor = new EADMetadataRecordExtractor(
                                parentPid, siblingItem, itemSep, ead, archival);
                        recordExtractors.add(recordExtractor);
                    }
                }
            }
        }
        catch (XPathExpressionException ignored) {
            LOGGER.debug("getRecordExtractorsForContainerSiblings(): Invalid XPath", ignored);
        }

        return recordExtractors;
    }

    private Node findItemNode() {
        try {
            if (item == null)
                return null;

            Node itemNode = (Node) xpath.evaluate(
                    ".//ead:dsc//ead:unitid[normalize-space(text())='" + item + "']",
                    ead, XPathConstants.NODE);
            Node parentNode = (Node) xpParent.evaluate(itemNode, XPathConstants.NODE);

            // An item node is only valid if is a leaf item node (has no children with items)
            if (((NodeList) xpUnitId.evaluate(parentNode, XPathConstants.NODESET)).getLength() == 1)
                return parentNode;
            return null;
        }
        catch (XPathExpressionException ex) {
            return null;
        }
    }

    private ExternalRecordInfo.Restriction evaluateRestriction() {
        try {
            Element accessAndUse = (Element) xpAccessAndUse.evaluate(ead, XPathConstants.NODE);
            Element accessRestrict = (Element) xpAccessRestrict.evaluate(accessAndUse, XPathConstants.NODE);
            String restriction = xpP.evaluate(accessRestrict);

            String type = accessRestrict.getAttribute("type").toLowerCase();
            if (type.equals("date"))
                restriction = "date";

            Node itemNode = findItemNode();
            if (type.equals("part") && (itemNode != null)) {
                accessRestrict = (Element) xpAccessRestrict.evaluate(itemNode, XPathConstants.NODE);
                if (accessRestrict != null)
                    restriction = accessRestrict.getAttribute("type");
                else
                    restriction = "open";
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

    private Inventory getInventory() {
        try {
            if (this.item == null && xpInventory.evaluate(ead, XPathConstants.NODE) != null)
                return getInventory(ead);

            return null;
        }
        catch (XPathExpressionException ignored) {
            LOGGER.debug("getInventory(): Invalid XPath", ignored);
            return null;
        }
    }

    private Inventory getInventory(Node itemNode) throws XPathExpressionException {
        Inventory inventory = new Inventory();
        inventory.setUnitId(XmlUtils.evaluate(xpUnitIdItem, itemNode));
        inventory.setTitle(XmlUtils.evaluate(xpTitleItem, itemNode));

        List<Inventory> children = new ArrayList<>();
        inventory.setChildren(children);

        if (itemNode == ead) {
            itemNode = (Node) xpInventory.evaluate(ead, XPathConstants.NODE);
            itemNode = itemNode.cloneNode(true);

            inventory.setUnitId(XmlUtils.evaluate(xpUnitId, ead));
            inventory.setTitle(XmlUtils.evaluate(xpTitle, ead));
        }
        else {
            // Detach this node from the parent to speed up the processing of the inventory
            itemNode.getParentNode().removeChild(itemNode);
        }

        NodeList itemNodes = (NodeList) xpChildren.evaluate(itemNode, XPathConstants.NODESET);
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Node childItem = itemNodes.item(i);
            children.add(getInventory(childItem));
        }

        return inventory;
    }
}
