package org.socialhistoryservices.delivery.api;

import java.net.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.socialhistoryservices.delivery.config.DeliveryProperties;

/**
 * Represents the api.socialhistoryservices.org lookup service.
 */
public class IISHRecordLookupService implements RecordLookupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IISHRecordLookupService.class);
    private static final String SRW_SEARCH_PATH = "ns1:recordData/marc:record/";

    private static final XPathExpression xpSearch, xpAll, xpOAI, xpSearch245aTitle, xpSearch500aTitle,
            xpSearch600aTitle, xpSearch610aTitle, xpSearch650aTitle, xpSearch651aTitle, xpSearch245kTitle,
            xpSearch245bSubTitle, xp856uUrl, xpSearchIdent, xpSearchMeta, xpNumberOfRecords;

    private DeliveryProperties deliveryProperties;

    static {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new IISHNamespaceContext());

        try {
            xpAll = xpath.compile("/srw:searchRetrieveResponse");
            xpOAI = xpath.compile("//oai:record");
            xpSearch = xpath.compile("//srw:record");
            xpSearch245aTitle = XmlUtils.getXPathForMarc(xpath, "245", 'a', SRW_SEARCH_PATH);
            xpSearch245bSubTitle = XmlUtils.getXPathForMarc(xpath, "245", 'b', SRW_SEARCH_PATH);
            xpSearch500aTitle = XmlUtils.getXPathForMarc(xpath, "500", 'a', SRW_SEARCH_PATH);
            xpSearch600aTitle = XmlUtils.getXPathForMarc(xpath, "600", 'a', SRW_SEARCH_PATH);
            xpSearch610aTitle = XmlUtils.getXPathForMarc(xpath, "610", 'a', SRW_SEARCH_PATH);
            xpSearch650aTitle = XmlUtils.getXPathForMarc(xpath, "650", 'a', SRW_SEARCH_PATH);
            xpSearch651aTitle = XmlUtils.getXPathForMarc(xpath, "651", 'a', SRW_SEARCH_PATH);
            xpSearch245kTitle = XmlUtils.getXPathForMarc(xpath, "245", 'k', SRW_SEARCH_PATH);
            xp856uUrl = XmlUtils.getXPathForMarc(xpath, "856", 'u');
            xpSearchIdent = xpath.compile("ns1:extraRecordData/extraData:extraData/iisg:identifier");
            xpSearchMeta = xpath.compile("//marc:record");
            xpNumberOfRecords = xpath.compile("//ns1:numberOfRecords");
        }
        catch (XPathExpressionException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Set the properties info.
     *
     * @param p The properties to set.
     */
    public void setDeliveryProperties(DeliveryProperties p) {
        deliveryProperties = p;
    }

    /**
     * Search for records with the specified title.
     *
     * @param title The title to search for.
     * @return A map of {pid,title} key-value pairs.
     */
    @Override
    public PageChunk getRecordsByTitle(String title, int resultCountPerChunk, int resultStart) {
        PageChunk pc = new PageChunk(resultCountPerChunk, resultStart);
        if (title == null) return pc;

        title = URLEncoder.encode(title, StandardCharsets.UTF_8);

        String query = getQuery("marc.245+all+\"" + title + "\"", true);
        LOGGER.debug(String.format("getRecordsByTitle(title: %s, resultcountPerChunk: %d, resultStart: %d)",
                title, resultCountPerChunk, resultStart));
        Node out = doSearch(query, pc.getResultCountPerChunk(), pc.getResultStart());

        NodeList search;
        try {
            search = (NodeList) xpSearch.evaluate(out, XPathConstants.NODESET);
            pc.setTotalResultCount(((Double) xpNumberOfRecords.evaluate(out, XPathConstants.NUMBER)).intValue());
        }
        catch (XPathExpressionException e) {
            LOGGER.debug("getRecordsByTitle(): Invalid XPath", e);
            return pc;
        }

        if (search.getLength() == 0) {
            return pc;
        }

        int resCount = search.getLength();
        for (int i = 0; i < resCount; ++i) {
            Node node = search.item(i);

            String recPid, recTitle;
            try {
                recPid = xpSearchIdent.evaluate(node);
                recTitle = evaluateSearchTitle(node);
            }
            catch (XPathExpressionException ex) {
                continue;
            }

            String recSubTitle = "";
            try {
                recSubTitle = " " + xpSearch245bSubTitle.evaluate(node).trim().replaceAll("[/:]$", "");
            }
            catch (XPathExpressionException ignored) {
            }

            if (recTitle != null && recPid != null) {
                // Strip trailing slashes.
                recTitle = recTitle.trim().replaceAll("[/:]$", "");
                if (!recTitle.isEmpty()) pc.getResults().put(recPid, recTitle + recSubTitle);
            }
        }
        return pc;
    }

    /**
     * Maps a PID to a record metadata extractor.
     *
     * @param pid The PID to lookup.
     * @return The metadata extractor of the record, if found.
     * @throws NoSuchPidException Thrown when the PID is not found.
     */
    @Override
    public MetadataRecordExtractor getRecordExtractorByPid(String pid) throws NoSuchPidException {
        LOGGER.debug(String.format("getRecordExtractorByPid(%s)", pid));

        String[] parentPidAndItem = getParentPidAndItem(pid);
        Node node = searchByPid(parentPidAndItem[0], true);
        Node eadNode = getEADNode(node);

        if (eadNode != null) {
            Node archivalNode = (parentPidAndItem[1] == null)
                    ? searchByPid(parentPidAndItem[0], false) : null;

            return new EADMetadataRecordExtractor(parentPidAndItem[0], parentPidAndItem[1],
                    deliveryProperties.getItemSeparator(), eadNode, archivalNode);
        }

        return new MARCMetadataRecordExtractor(pid, node);
    }

    /**
     * Actually execute the search.
     *
     * @param query            The query to add to the search.
     * @param nrResultsPerPage The number of results to get per page.
     * @param resultStart      The result number to start the page with (1 <= resultStart <= result count).
     * @return A list of nodes returned by the API service.
     */
    private Node doSearch(String query, int nrResultsPerPage, int resultStart) {
        String search;
        search = "version=1.1";
        search += "&operation=searchRetrieve";
        search += "&recordSchema=info:srw/schema/1/marcxml-v1.1";
        search += "&maximumRecords=" + nrResultsPerPage;
        search += "&startRecord=" + resultStart;
        search += "&resultSetTTL=300";
        search += "&recordPacking=xml";
        search += "&sortKeys=";
        search += "&stylesheet=";
        search += "&query=" + query;

        try {
            String apiProto = deliveryProperties.getApiProto();
            String apiDomain = deliveryProperties.getApiDomain();
            String apiBase = deliveryProperties.getApiBase();
            int apiPort = deliveryProperties.getApiPort();

            URI uri = new URI(apiProto, null, apiDomain, apiPort, apiBase, search, null);
            URL req = uri.toURL();
            LOGGER.debug(String.format("doSearch(): Querying SRW API: %s", req.toString()));
            URLConnection conn = req.openConnection();

            BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return (Node) xpAll.evaluate(new InputSource(rdr), XPathConstants.NODE);
        }
        catch (IOException ex) {
            LOGGER.debug("doSearch(): API Connect Failed", ex);
            return null;
        }
        catch (URISyntaxException ex) {
            LOGGER.debug("doSearch(): Invalid URI syntax", ex);
            return null;
        }
        catch (XPathExpressionException e) {
            LOGGER.debug("doSearch(): Invalid XPath", e);
            return null;
        }
    }

    /**
     * Search metadata by PID.
     *
     * @param pid      The PID to search for.
     * @param metadata Whether we want the metadata record.
     * @return The main record node.
     * @throws NoSuchPidException Thrown when the search returns nothing.
     */
    private Node searchByPid(String pid, boolean metadata) throws NoSuchPidException {
        // If we do not search for metadata, then we need to strip the naming authority from the PID
        if (!metadata) {
            pid = pid.replace("10622/", "");
        }

        String encodedPid;
        encodedPid = URLEncoder.encode(pid, StandardCharsets.UTF_8);

        String query = metadata
                ? getQuery("dc.identifier+=+\"" + encodedPid + "\"", true)
                : getQuery("marc.852$j=+\"" + encodedPid + "\"", false);

        Node all = doSearch(query, 1, 1);
        NodeList search;
        int resultCount;

        try {
            resultCount = ((Double) xpNumberOfRecords.evaluate(all, XPathConstants.NUMBER)).intValue();
            search = (NodeList) xpSearchMeta.evaluate(all, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            LOGGER.debug("searchByPid(): Invalid XPath", e);
            throw new NoSuchPidException();
            // Handle this in case the IISH API is down.
        }

        if (resultCount == 0 || search == null) {
            LOGGER.debug("searchByPid(): Zero results");
            throw new NoSuchPidException();
        }

        return search.item(0);
    }

    private String[] getParentPidAndItem(String pid) {
        String itemSeparator = deliveryProperties.getItemSeparator();
        if (pid.contains(itemSeparator)) {
            int idx = pid.indexOf(itemSeparator);
            String parentPid = pid.substring(0, idx);
            String item = pid.substring(idx + 1);
            return new String[]{parentPid, item};
        }
        return new String[]{pid, null};
    }

    private String evaluateSearchTitle(Node node) throws XPathExpressionException {
        String recTitle;
        recTitle = xpSearch245aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch500aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch600aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch610aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch650aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch651aTitle.evaluate(node);
        if (recTitle.isEmpty())
            recTitle = xpSearch245kTitle.evaluate(node);
        return recTitle;
    }

    private String getQuery(String q, boolean metadata) {
        String query;

        if (metadata) {
            query = "iisg.collectionName+=+\"iish.evergreen.biblio\"";
            query += "+or+iisg.collectionName+=+\"iish.archieven\"";
            query += "+or+iisg.collectionName+=+\"iish.eci\"";
        }
        else {
            query = "iisg.collectionName+=+\"iish.evergreen.biblio\"";
        }

        if (!q.isEmpty()) {
            query += "+and+" + q;
        }

        return query;
    }

    private Node getEADNode(Node node) throws NoSuchPidException {
        try {
            String url = xp856uUrl.evaluate(node);
            if (url.endsWith("?locatt=view:ead")) {
                URL eadUrl = new URL(url);
                LOGGER.debug(String.format("getEADNode(): Querying EAD URL: %s", eadUrl.toString()));
                HttpURLConnection conn = (HttpURLConnection) eadUrl.openConnection();

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    url = conn.getHeaderField("Location");
                    URL redirectUrl = new URL(url);
                    conn = (HttpURLConnection) redirectUrl.openConnection();
                }

                BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                return (Node) xpOAI.evaluate(new InputSource(rdr), XPathConstants.NODE);
            }
            return null;
        }
        catch (IOException ex) {
            LOGGER.debug("getEADNode(): API Connect Failed", ex);
            throw new NoSuchPidException();
        }
        catch (XPathExpressionException ex) {
            LOGGER.debug("getEADNode(): Invalid XPath", ex);
            throw new NoSuchPidException();
        }
    }
}
