package org.socialhistoryservices.delivery.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.*;

public class MARCRecordExtractor implements IISHRecordExtractor {
    private static final Log logger = LogFactory.getLog(MARCRecordExtractor.class);

    private XPathExpression xpAuthor, xpAltAuthor, xpAlt2Author, xpAlt3Author, xp245aTitle, xp500aTitle, xp600aTitle,
            xp610aTitle, xp650aTitle, xp651aTitle, xp245kTitle, xp245bSubTitle, xpYear, xpPhysicalDescription, xpGenres,
            xpShelvingLocations, xpSerialNumbers, xpSignatures, xpBarcodes, xpLeader, xp540bCopyright, xp542mAccess;

    public MARCRecordExtractor() {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new IISHNamespaceContext());

        try {
            xpAuthor = XmlUtils.getXPathForMarc(xpath, "100", 'a');
            xpAltAuthor = XmlUtils.getXPathForMarc(xpath, "110", 'a');
            xpAlt2Author = XmlUtils.getXPathForMarc(xpath, "700", 'a');
            xpAlt3Author = XmlUtils.getXPathForMarc(xpath, "710", 'a');
            xp245aTitle = XmlUtils.getXPathForMarc(xpath, "245", 'a');
            xp500aTitle = XmlUtils.getXPathForMarc(xpath, "500", 'a');
            xp600aTitle = XmlUtils.getXPathForMarc(xpath, "600", 'a');
            xp610aTitle = XmlUtils.getXPathForMarc(xpath, "610", 'a');
            xp650aTitle = XmlUtils.getXPathForMarc(xpath, "650", 'a');
            xp651aTitle = XmlUtils.getXPathForMarc(xpath, "651", 'a');
            xp245kTitle = XmlUtils.getXPathForMarc(xpath, "245", 'k');
            xp245bSubTitle = XmlUtils.getXPathForMarc(xpath, "245", 'b');
            xpYear = XmlUtils.getXPathForMarc(xpath, "260", 'c');
            xpPhysicalDescription = XmlUtils.getXPathForMarc(xpath, "300", 'a');
            xpGenres = XmlUtils.getXPathForMarc(xpath, "655", 'a');
            xpShelvingLocations = XmlUtils.getXPathForMarc(xpath, "852", 'c');
            xpSignatures = XmlUtils.getXPathForMarc(xpath, "852", 'j');
            xpBarcodes = XmlUtils.getXPathForMarc(xpath, "852", 'p');
            xpSerialNumbers = XmlUtils.getXPathForMarc(xpath, "866", 'a');
            xpLeader = xpath.compile("marc:leader");
            xp540bCopyright = XmlUtils.getXPathForMarc(xpath, "540", 'b');
            xp542mAccess = XmlUtils.getXPathForMarc(xpath, "542", 'm');
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
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();

        String author = evaluateAuthor(node);
        if (author != null && !author.isEmpty()) {
            externalInfo.setAuthor(stripToSize(author, 125));
        }

        String title = evaluateTitle(node);
        if (title != null && !title.isEmpty()) {
            // Strip trailing slashes
            title = title.trim().replaceAll("[/:]$", "");
            String subTitle = XmlUtils.evaluate(xp245bSubTitle, node);
            if (subTitle != null && !subTitle.isEmpty()) {
                title += " " + subTitle.trim().replaceAll("[/:]$", "");
            }

            // Some titles from the API SRW exceed 255 characters.
            // Strip them to 251 characters (up to 252 , exclusive) to save up for the dash and \0 termination.
            // EDIT: Trim this to ~125 characters for readability (this is the current max size of the field).
            title = stripToSize(title, 125);
            externalInfo.setTitle(title);
        } else {
            externalInfo.setTitle("Unknown Record");
        }

        String year = XmlUtils.evaluate(xpYear, node);
        if (year != null && !year.isEmpty()) {
            externalInfo.setDisplayYear(stripToSize(year, 30));
        }

        externalInfo.setMaterialType(evaluateMaterialType(node));
        externalInfo.setCopyright(XmlUtils.evaluate(xp540bCopyright, node));
        externalInfo.setPublicationStatus(evaluatePublicationStatus(node));
        externalInfo.setPhysicalDescription(XmlUtils.evaluate(xpPhysicalDescription, node));
        externalInfo.setGenres(evaluateGenres(node));
        externalInfo.setRestriction(ExternalRecordInfo.Restriction.OPEN);

        return externalInfo;
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
        return getRecordMetadata(node);
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
        Map<String, ExternalHoldingInfo> retMap = new HashMap<>();

        try {
            // TODO: 866 is not always available.
            NodeList shelfNodes = (NodeList) xpShelvingLocations.evaluate(node, XPathConstants.NODESET);
            NodeList sigNodes = (NodeList) xpSignatures.evaluate(node, XPathConstants.NODESET);
            NodeList serNodes = (NodeList) xpSerialNumbers.evaluate(node, XPathConstants.NODESET);
            NodeList barcodes = (NodeList) xpBarcodes.evaluate(node, XPathConstants.NODESET);

            if (shelfNodes == null || sigNodes == null || serNodes == null || barcodes == null) {
                return retMap;
            }

            for (int i = 0; i < sigNodes.getLength(); i++) {
                Node shelf = shelfNodes.item(i);
                Node sig = sigNodes.item(i);
                Node ser = serNodes.item(i);
                Node barcode = barcodes.item(i);

                if (sig == null) {
                    continue;
                }

                ExternalHoldingInfo eh = new ExternalHoldingInfo();
                if (shelf != null) {
                    eh.setShelvingLocation(shelf.getTextContent());
                }
                if (barcode != null) {
                    eh.setBarcode(barcode.getTextContent());
                }
                if (ser != null) {
                    eh.setSerialNumbers(ser.getTextContent().replace(",", ", "));
                }
                retMap.put(sig.getTextContent(), eh);
            }
        }
        catch (XPathExpressionException ignored) {
            logger.debug("getHoldingMetadata(): Invalid XPath", ignored);
        }

        return retMap;
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
        return getHoldingMetadata(node);
    }

    private String stripToSize(String string, int size) {
        if (string.length() > size) {
            string = string.substring(0, size);
            string = string.trim();
        }
        return string;
    }

    private ExternalRecordInfo.MaterialType evaluateMaterialType(Node node) {
        try {
            String leader = xpLeader.evaluate(node);
            String titleForm = xp245kTitle.evaluate(node);
            return leaderToMaterialType(leader, titleForm);
        }
        catch (XPathExpressionException e) {
            return ExternalRecordInfo.MaterialType.OTHER;
        }
    }

    private ExternalRecordInfo.MaterialType leaderToMaterialType(String leader, String titleForm) {
        String format = leader.substring(6, 8);
        String coll = titleForm.trim().toLowerCase();

        if (format.equals("ab")) {
            return ExternalRecordInfo.MaterialType.ARTICLE;
        }

        if (format.equals("ar") || format.equals("as") || format.equals("ps")) {
            return ExternalRecordInfo.MaterialType.SERIAL;
        }

        if (format.equals("am") || format.equals("pm")) {
            return ExternalRecordInfo.MaterialType.BOOK;
        }

        if (format.equals("im") || format.equals("pi") || format.equals("ic") || format.equals("jm")) {
            return ExternalRecordInfo.MaterialType.SOUND;
        }

        if (format.equals("do") || format.equals("oc")) {
            return ExternalRecordInfo.MaterialType.DOCUMENTATION;
        }

        if (format.equals("bm") || format.equals("pc")) {
            return ExternalRecordInfo.MaterialType.ARCHIVE;
        }

        if (format.equals("av") || format.equals("rm") || format.equals("pv")
                || format.equals("km") || format.equals("kc")) {
            return ExternalRecordInfo.MaterialType.VISUAL;
        }

        if (format.equals("ac") && coll.contains("book collection")) {
            return ExternalRecordInfo.MaterialType.BOOK;
        }

        if (format.equals("ac") && coll.contains("serial collection")) {
            return ExternalRecordInfo.MaterialType.SERIAL;
        }

        if (format.equals("pc") && coll.contains("archief")) {
            return ExternalRecordInfo.MaterialType.ARCHIVE;
        }

        if (format.equals("pc") && coll.contains("archive")) {
            return ExternalRecordInfo.MaterialType.ARCHIVE;
        }

        if (format.equals("pc") && coll.contains("collection")) {
            return ExternalRecordInfo.MaterialType.DOCUMENTATION;
        }

        if (format.equals("gm") && coll.contains("moving image document")) {
            return ExternalRecordInfo.MaterialType.MOVING_VISUAL;
        }

        if (format.equals("gc") && coll.contains("moving image collection")) {
            return ExternalRecordInfo.MaterialType.MOVING_VISUAL;
        }

        if (format.equals("kc") && coll.contains("poster collection")) {
            return ExternalRecordInfo.MaterialType.VISUAL;
        }

        if (format.equals("rc") && coll.contains("object collection")) {
            return ExternalRecordInfo.MaterialType.VISUAL;
        }

        if (format.equals("jc") && coll.contains("music collection")) {
            return ExternalRecordInfo.MaterialType.SOUND;
        }

        return ExternalRecordInfo.MaterialType.OTHER;
    }

    private String evaluateGenres(Node node) {
        try {
            Set<String> genres = new HashSet<>();
            NodeList nodeList = (NodeList) xpGenres.evaluate(node, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String genre = nodeList.item(i).getTextContent();
                genre = genre.toLowerCase().trim();
                if (genre.endsWith(".")) {
                    genre = genre.substring(0, genre.length() - 1).trim();
                }
                genres.add(genre);
            }
            return (!genres.isEmpty()) ? StringUtils.collectionToDelimitedString(genres, ",") : null;
        }
        catch (XPathExpressionException e) {
            return null;
        }
    }

    private String evaluateTitle(Node node) {
        try {
            String title = xp245aTitle.evaluate(node);

            if (title.isEmpty()) {
                title = xp500aTitle.evaluate(node);
            }
            if (title.isEmpty()) {
                title = xp600aTitle.evaluate(node);
            }
            if (title.isEmpty()) {
                title = xp610aTitle.evaluate(node);
            }
            if (title.isEmpty()) {
                title = xp650aTitle.evaluate(node);
            }
            if (title.isEmpty()) {
                title = xp651aTitle.evaluate(node);
            }
            if (title.isEmpty()) {
                title = xp245kTitle.evaluate(node);
            }

            // Strip trailing slashes
            title = title.trim().replaceAll("[/:]$", "");
            String subTitle = XmlUtils.evaluate(xp245bSubTitle, node);
            if (subTitle != null && !subTitle.isEmpty()) {
                title += " " + subTitle.trim().replaceAll("[/:]$", "");
            }

            return title;
        }
        catch (XPathExpressionException e) {
            return null;
        }
    }

    /**
     * Fetches author from MARCXML, first tries 100a, then 110a.
     *
     * @param node The XML node to execute the XPath on.
     * @return The author found, or null if not present.
     */
    private String evaluateAuthor(Node node) {
        try {
            String author = xpAuthor.evaluate(node);
            if (author.isEmpty()) {
                author = xpAltAuthor.evaluate(node);
            }
            if (author.isEmpty()) {
                author = xpAlt2Author.evaluate(node);
            }
            if (author.isEmpty()) {
                author = xpAlt3Author.evaluate(node);
            }
            return author;
        }
        catch (XPathExpressionException ex) {
            return null;
        }
    }

    /**
     * Fetches publication status from MARCXML.
     *
     * @param node The XML node to execute the XPath on.
     * @return The publication status.
     */
    private ExternalRecordInfo.PublicationStatus evaluatePublicationStatus(Node node) {
        try {
            String status = xp542mAccess.evaluate(node);

            ExternalRecordInfo.PublicationStatus publicationStatus = ExternalRecordInfo.PublicationStatus.UNKNOWN;
            if (status.trim().equalsIgnoreCase("irsh")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.IRSH;
            }
            if (status.trim().equalsIgnoreCase("open")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.OPEN;
            }
            if (status.trim().equalsIgnoreCase("restricted")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.RESTRICTED;
            }
            if (status.trim().equalsIgnoreCase("minimal")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.MINIMAL;
            }
            if (status.trim().equalsIgnoreCase("pictoright")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.PICTORIGHT;
            }
            if (status.trim().equalsIgnoreCase("closed")) {
                publicationStatus = ExternalRecordInfo.PublicationStatus.CLOSED;
            }

            return publicationStatus;
        }
        catch (XPathExpressionException ex) {
            return ExternalRecordInfo.PublicationStatus.UNKNOWN;
        }
    }

    @Override
    public String getUnitIdsFromContainer(Node node, String pid, String container, boolean includeCurrentContainer) throws NoSuchPidException {
        return null;
    }
}
