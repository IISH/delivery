package org.socialhistoryservices.delivery.api;

import java.util.*;
import javax.xml.xpath.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.util.StringUtils;

import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;

public class MARCMetadataRecordExtractor implements MetadataRecordExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(MARCMetadataRecordExtractor.class);

    private static final XPathExpression xpAuthor, xpAltAuthor, xpAlt2Author, xpAlt3Author, xp245aTitle,
            xp500aTitle, xp600aTitle, xp610aTitle, xp650aTitle, xp651aTitle, xp245kTitle, xp245bSubTitle, xpYear,
            xpPhysicalDescription, xpGenres, xpShelvingLocations, xpSerialNumbers, xpSignatures, xpBarcodes,
            xpLeader, xp540bCopyright, xp542mAccess;

    private final String pid;
    private final Node marc;

    static {
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
            throw new RuntimeException(ex);
        }
    }

    public MARCMetadataRecordExtractor(String pid, Node marc) {
        this.pid = pid;
        this.marc = marc;
    }

    /**
     * Returns the PID of the record.
     *
     * @return The PID.
     */
    @Override
    public String getPid() {
        return pid;
    }

    /**
     * Extracts the metadata of the record.
     *
     * @return The metadata of the record, if found.
     */
    @Override
    public ExternalRecordInfo getRecordMetadata() {
        ExternalRecordInfo externalInfo = new ExternalRecordInfo();

        String author = evaluateAuthor();
        if (author != null && !author.isEmpty()) {
            externalInfo.setAuthor(MetadataRecordExtractor.stripToSize(author, 125));
        }

        String title = evaluateTitle();
        if (title != null && !title.isEmpty()) {
            // Strip trailing slashes
            title = title.trim().replaceAll("[/:]$", "");
            String subTitle = XmlUtils.evaluate(xp245bSubTitle, marc);
            if (subTitle != null && !subTitle.isEmpty()) {
                title += " " + subTitle.trim().replaceAll("[/:]$", "");
            }

            // Some titles from the API SRW exceed 255 characters.
            // Strip them to 251 characters (up to 252 , exclusive) to save up for the dash and \0 termination.
            // EDIT: Trim this to ~125 characters for readability (this is the current max size of the field).
            title = MetadataRecordExtractor.stripToSize(title, 125);
            externalInfo.setTitle(title);
        }
        else {
            externalInfo.setTitle("Unknown Record");
        }

        String year = XmlUtils.evaluate(xpYear, marc);
        if (year != null && !year.isEmpty()) {
            externalInfo.setDisplayYear(MetadataRecordExtractor.stripToSize(year, 30));
        }

        externalInfo.setMaterialType(evaluateMaterialType());
        externalInfo.setCopyright(XmlUtils.evaluate(xp540bCopyright, marc));
        externalInfo.setPublicationStatus(evaluatePublicationStatus());
        externalInfo.setPhysicalDescription(XmlUtils.evaluate(xpPhysicalDescription, marc));
        externalInfo.setGenres(evaluateGenres());
        externalInfo.setRestriction(ExternalRecordInfo.Restriction.OPEN);

        return externalInfo;
    }

    /**
     * Get a map of holding signatures associated with this this marc,
     * linking to additional holding info provided by the API.
     *
     * @return A map of found (signature,holding info) tuples, or an empty map if none were found.
     */
    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadata() {
        Map<String, ExternalHoldingInfo> retMap = new HashMap<>();

        try {
            // TODO: 866 is not always available.
            NodeList shelfNodes = (NodeList) xpShelvingLocations.evaluate(marc, XPathConstants.NODESET);
            NodeList sigNodes = (NodeList) xpSignatures.evaluate(marc, XPathConstants.NODESET);
            NodeList serNodes = (NodeList) xpSerialNumbers.evaluate(marc, XPathConstants.NODESET);
            NodeList barcodes = (NodeList) xpBarcodes.evaluate(marc, XPathConstants.NODESET);

            if (shelfNodes == null || sigNodes == null || serNodes == null || barcodes == null)
                return retMap;

            for (int i = 0; i < sigNodes.getLength(); i++) {
                Node shelf = shelfNodes.item(i);
                Node sig = sigNodes.item(i);
                Node ser = serNodes.item(i);
                Node barcode = barcodes.item(i);

                if (sig == null) continue;

                ExternalHoldingInfo eh = new ExternalHoldingInfo();
                if (shelf != null)
                    eh.setShelvingLocation(shelf.getTextContent());
                if (barcode != null)
                    eh.setBarcode(barcode.getTextContent());
                if (ser != null)
                    eh.setSerialNumbers(ser.getTextContent().replace(",", ", "));
                retMap.put(sig.getTextContent(), eh);
            }
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
        return Collections.emptyList();
    }

    /**
     * Obtains metadata record extractors for all container siblings of the current record.
     * These records do not only share the same parent record, but also share a common container.
     *
     * @return A set of metadata record extractors for the container siblings.
     */
    public Set<MetadataRecordExtractor> getRecordExtractorsForContainerSiblings() {
        return Collections.emptySet();
    }

    private ExternalRecordInfo.MaterialType evaluateMaterialType() {
        try {
            String leader = xpLeader.evaluate(marc);
            String titleForm = xp245kTitle.evaluate(marc);
            return leaderToMaterialType(leader, titleForm);
        }
        catch (XPathExpressionException e) {
            return ExternalRecordInfo.MaterialType.OTHER;
        }
    }

    private ExternalRecordInfo.MaterialType leaderToMaterialType(String leader, String titleForm) {
        String format = leader.substring(6, 8);
        String coll = titleForm.trim().toLowerCase();

        if (format.equals("ab"))
            return ExternalRecordInfo.MaterialType.ARTICLE;

        if (format.equals("ar") || format.equals("as") || format.equals("ps"))
            return ExternalRecordInfo.MaterialType.SERIAL;

        if (format.equals("am") || format.equals("pm"))
            return ExternalRecordInfo.MaterialType.BOOK;

        if (format.equals("im") || format.equals("pi") || format.equals("ic") || format.equals("jm"))
            return ExternalRecordInfo.MaterialType.SOUND;

        if (format.equals("do") || format.equals("oc"))
            return ExternalRecordInfo.MaterialType.DOCUMENTATION;

        if (format.equals("bm") || format.equals("pc"))
            return ExternalRecordInfo.MaterialType.ARCHIVE;

        if (format.equals("av") || format.equals("rm") || format.equals("pv")
                || format.equals("km") || format.equals("kc"))
            return ExternalRecordInfo.MaterialType.VISUAL;

        if (format.equals("ac") && coll.contains("book collection"))
            return ExternalRecordInfo.MaterialType.BOOK;

        if (format.equals("ac") && coll.contains("serial collection"))
            return ExternalRecordInfo.MaterialType.SERIAL;

        if (format.equals("gm") && coll.contains("moving image document"))
            return ExternalRecordInfo.MaterialType.MOVING_VISUAL;

        if (format.equals("gc") && coll.contains("moving image collection"))
            return ExternalRecordInfo.MaterialType.MOVING_VISUAL;

        if (format.equals("rc") && coll.contains("object collection"))
            return ExternalRecordInfo.MaterialType.VISUAL;

        if (format.equals("jc") && coll.contains("music collection"))
            return ExternalRecordInfo.MaterialType.SOUND;

        return ExternalRecordInfo.MaterialType.OTHER;
    }

    private String evaluateGenres() {
        try {
            Set<String> genres = new HashSet<>();
            NodeList nodeList = (NodeList) xpGenres.evaluate(marc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                String genre = nodeList.item(i).getTextContent();
                genre = genre.toLowerCase().trim();
                if (genre.endsWith("."))
                    genre = genre.substring(0, genre.length() - 1).trim();
                genres.add(genre);
            }
            return (!genres.isEmpty()) ? StringUtils.collectionToDelimitedString(genres, ",") : null;
        }
        catch (XPathExpressionException e) {
            return null;
        }
    }

    private String evaluateTitle() {
        try {
            String title = xp245aTitle.evaluate(marc);

            if (title.isEmpty())
                title = xp500aTitle.evaluate(marc);
            if (title.isEmpty())
                title = xp600aTitle.evaluate(marc);
            if (title.isEmpty())
                title = xp610aTitle.evaluate(marc);
            if (title.isEmpty())
                title = xp650aTitle.evaluate(marc);
            if (title.isEmpty())
                title = xp651aTitle.evaluate(marc);
            if (title.isEmpty())
                title = xp245kTitle.evaluate(marc);

            // Strip trailing slashes
            title = title.trim().replaceAll("[/:]$", "");
            String subTitle = XmlUtils.evaluate(xp245bSubTitle, marc);
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
     * @return The author found, or null if not present.
     */
    private String evaluateAuthor() {
        try {
            String author = xpAuthor.evaluate(marc);
            if (author.isEmpty())
                author = xpAltAuthor.evaluate(marc);
            if (author.isEmpty())
                author = xpAlt2Author.evaluate(marc);
            if (author.isEmpty())
                author = xpAlt3Author.evaluate(marc);
            return author;
        }
        catch (XPathExpressionException ex) {
            return null;
        }
    }

    /**
     * Fetches publication status from MARCXML.
     *
     * @return The publication status.
     */
    private ExternalRecordInfo.PublicationStatus evaluatePublicationStatus() {
        try {
            String status = xp542mAccess.evaluate(marc);

            ExternalRecordInfo.PublicationStatus publicationStatus = ExternalRecordInfo.PublicationStatus.UNKNOWN;
            if (status.trim().equalsIgnoreCase("irsh"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.IRSH;
            if (status.trim().equalsIgnoreCase("open"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.OPEN;
            if (status.trim().equalsIgnoreCase("restricted"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.RESTRICTED;
            if (status.trim().equalsIgnoreCase("minimal"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.MINIMAL;
            if (status.trim().equalsIgnoreCase("pictoright"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.PICTORIGHT;
            if (status.trim().equalsIgnoreCase("closed"))
                publicationStatus = ExternalRecordInfo.PublicationStatus.CLOSED;

            return publicationStatus;
        }
        catch (XPathExpressionException ex) {
            return ExternalRecordInfo.PublicationStatus.UNKNOWN;
        }
    }
}
