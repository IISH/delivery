/**
 * Copyright (C) 2013 International Institute of Social History
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.socialhistoryservices.delivery.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Represents the api.socialhistoryservices.nl lookup service.
 */
public class IISHRecordLookupService implements RecordLookupService {
    private static final Log logger = LogFactory.getLog(IISHRecordLookupService.class);

    private Properties properties;

    private XPathExpression xpSearch, xpAll, xpOAI, xpSearch245aTitle, xpSearch500aTitle, xpSearch600aTitle,
        xpSearch610aTitle, xpSearch650aTitle, xpSearch651aTitle, xpSearch245kTitle, xpSearch245bSubTitle,
        xpArchive931, xpArchiveLocation, xpArchiveMeter, xpArchiveNumbers, xpArchiveFormat, xpArchiveNote,
        xp856uUrl, xpSearchIdent, xpSearchMeta, xpNumberOfRecords;

    private IISHRecordExtractor marcRecordExtractor, eadRecordExtractor;

    /**
     * Set the properties info.
     *
     * @param p The properties to set.
     */
    public void setProperties(Properties p) {
        properties = p;
    }

    /**
     * Constructor.
     */
    public IISHRecordLookupService() {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new IISHNamespaceContext());

        try {
            xpAll = xpath.compile("/srw:searchRetrieveResponse");
            xpOAI = xpath.compile("//oai:record");
            xpSearch = xpath.compile("//srw:record");
            xpSearch245aTitle = XmlUtils.getXPathForMarc(xpath, "245", 'a');
            xpSearch245bSubTitle = XmlUtils.getXPathForMarc(xpath, "245", 'b');
            xpSearch500aTitle = XmlUtils.getXPathForMarc(xpath, "500", 'a');
            xpSearch600aTitle = XmlUtils.getXPathForMarc(xpath, "600", 'a');
            xpSearch610aTitle = XmlUtils.getXPathForMarc(xpath, "610", 'a');
            xpSearch650aTitle = XmlUtils.getXPathForMarc(xpath, "650", 'a');
            xpSearch651aTitle = XmlUtils.getXPathForMarc(xpath, "651", 'a');
            xpSearch245kTitle = XmlUtils.getXPathForMarc(xpath, "245", 'k');
            xpArchive931 = XmlUtils.getXPathForMarcTag(xpath, "931");
            xpArchiveLocation = XmlUtils.getXPathForMarcSubfield(xpath, 'a');
            xpArchiveMeter = XmlUtils.getXPathForMarcSubfield(xpath, 'b');
            xpArchiveNumbers = XmlUtils.getXPathForMarcSubfield(xpath, 'c');
            xpArchiveFormat = XmlUtils.getXPathForMarcSubfield(xpath, 'e');
            xpArchiveNote = XmlUtils.getXPathForMarcSubfield(xpath, 'f');
            xp856uUrl = XmlUtils.getXPathForMarc(xpath, "856", 'u');
            xpSearchIdent = xpath.compile("ns1:extraRecordData/extraData:extraData/iisg:identifier");
            xpSearchMeta = xpath.compile("//marc:record");
            xpNumberOfRecords = xpath.compile("//ns1:numberOfRecords");
        }
        catch (XPathExpressionException ex) {
            logger.error("Failed initializing XPath expressions");
        }

        marcRecordExtractor = new MARCRecordExtractor();
        eadRecordExtractor = new EADRecordExtractor();
    }

    /**
     * Search for records with the specified title.
     *
     * @param title The title to search for.
     * @return A map of {pid,title} key-value pairs.
     */
    public PageChunk getRecordsByTitle(String title, int resultCountPerChunk, int resultStart) {
        PageChunk pc = new PageChunk(resultCountPerChunk, resultStart);
        if (title == null) return pc;

        try {
            title = URLEncoder.encode(title, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String query = getQuery("marc.245+all+\"" + title + "\"", true);
        logger.debug(String.format("getRecordsByTitle(title: %s, resultcountPerChunk: %d, resultStart: %d)",
            title, resultCountPerChunk, resultStart));
        Node out = doSearch(query, pc.getResultCountPerChunk(), pc.getResultStart());

        NodeList search = null;
        try {
            search = (NodeList) xpSearch.evaluate(out, XPathConstants.NODESET);
            pc.setTotalResultCount(((Double) xpNumberOfRecords.evaluate(out, XPathConstants.NUMBER)).intValue());
        }
        catch (XPathExpressionException e) {
            logger.debug("getRecordsByTitle(): Invalid XPath", e);
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

    @Override
    public ExternalRecordInfo getRecordMetaDataByPid(String pid) throws NoSuchPidException {
        logger.debug(String.format("getRecordMetaDataByPid(%s)", pid));

        String[] parentPidAndItem = getParentPidAndItem(pid);
        Node node = searchByPid(parentPidAndItem[0], true);
        Node eadNode = getEADNode(node);

        if (eadNode != null)
            return eadRecordExtractor.getRecordMetadata(eadNode, parentPidAndItem[1]);
        return marcRecordExtractor.getRecordMetadata(node);
    }

    @Override
    public List<ArchiveHoldingInfo> getArchiveHoldingInfoByPid(String pid) {
        logger.debug(String.format("getArchiveHoldingInfoByPid(%s)", pid));

        List<ArchiveHoldingInfo> info = new ArrayList<>();
        String[] parentPidAndItem = getParentPidAndItem(pid);

        // Child records should look for archive holding info at their parent
        if (parentPidAndItem[1] == null) {
            try {
                Node node = searchByPid(parentPidAndItem[0], false);
                NodeList archiveList = (NodeList) xpArchive931.evaluate(node, XPathConstants.NODESET);

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
                logger.debug("getArchiveHoldingInfoByPid(): Invalid XPath", ignored);
            }
            catch (NoSuchPidException ignored) {
                logger.debug("getArchiveHoldingInfoByPid(): No such PID", ignored);
            }
        }

        return info;
    }

    @Override
    public Map<String, ExternalHoldingInfo> getHoldingMetadataByPid(String pid) throws NoSuchPidException {
        logger.debug(String.format("getHoldingMetaDataByPid(%s)", pid));

        String[] parentPidAndItem = getParentPidAndItem(pid);
        Node node = searchByPid(parentPidAndItem[0], true);
        Node eadNode = getEADNode(node);

        if (eadNode != null)
            return eadRecordExtractor.getHoldingMetadata(eadNode, parentPidAndItem[1]);
        return marcRecordExtractor.getHoldingMetadata(node);
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
            String apiProto = properties.getProperty("prop_apiProto");
            String apiDomain = properties.getProperty("prop_apiDomain");
            String apiBase = properties.getProperty("prop_apiBase");
            int apiPort = Integer.parseInt(properties.getProperty("prop_apiPort"));

            URI uri = new URI(apiProto, null, apiDomain, apiPort, apiBase, search, null);
            URL req = uri.toURL();
            logger.debug(String.format("doSearch(): Querying SRW API: %s", req.toString()));
            URLConnection conn = req.openConnection();

            BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return (Node) xpAll.evaluate(new InputSource(rdr), XPathConstants.NODE);
        }
        catch (IOException ex) {
            logger.debug("doSearch(): API Connect Failed", ex);
            return null;
        }
        catch (URISyntaxException ex) {
            logger.debug("doSearch(): Invalid URI syntax", ex);
            return null;
        }
        catch (XPathExpressionException e) {
            logger.debug("doSearch(): Invalid XPath", e);
            return null;
        }
    }

    /**
     * Search metadata by PID.
     *
     * @param pid The PID to search for.
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
        try {
            encodedPid = URLEncoder.encode(pid, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String query;
        if (metadata) {
            query = getQuery("dc.identifier+=+\"" + encodedPid + "\"", true);
        }
        else {
            query = getQuery("marc.852$j=+\"" + encodedPid + "\"", false);
        }


        Node all = doSearch(query, 1, 1);
        NodeList search = null;
        int resultCount = 0;

        try {
            resultCount = ((Double) xpNumberOfRecords.evaluate(all, XPathConstants.NUMBER)).intValue();
            search = (NodeList) xpSearchMeta.evaluate(all, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            logger.debug("searchByPid(): Invalid XPath", e);
            throw new NoSuchPidException();
            // Handle this in case the IISH API is down.
        }

        if (resultCount == 0 || search == null) {
            logger.debug("searchByPid(): Zero results");
            throw new NoSuchPidException();
        }

        return search.item(0);
    }

    private String[] getParentPidAndItem(String pid) {
        String itemSeparator = properties.getProperty("prop_itemSeparator");
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

    private Node getEADNode(Node node) {
        try {
            String url = xp856uUrl.evaluate(node);
            if (url.endsWith("?locatt=view:ead")) {
                // TODO: Temp for testing purposes
                String id = url.replace("http://hdl.handle.net/10622/", "").replace("?locatt=view:ead", "");
                File file = new File("/home/ead/" + id  + ".xml");
                if (file.exists() && file.canRead()) {
                    FileInputStream fileInputStreamTemp = new FileInputStream(file);
                    BufferedReader rdrTemp = new BufferedReader(new InputStreamReader(fileInputStreamTemp));
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(new InputSource(rdrTemp));
                    Element eadNodeTemp = document.getDocumentElement();
                    if (eadNodeTemp != null) {
                        eadNodeTemp.setAttribute("ead","urn:isbn:1-931666-22-9");
                        return eadNodeTemp;
                    }
                }

                URL eadUrl = new URL(url);
                logger.debug(String.format("getEADNode(): Querying EAD URL: %s", eadUrl.toString()));
                URLConnection conn = eadUrl.openConnection();

                BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                Node eadNode = (Node) xpOAI.evaluate(new InputSource(rdr), XPathConstants.NODE);
                return eadNode;
            }
            return null;
        }
        catch (IOException ex) {
            logger.debug("getEADNode(): API Connect Failed", ex);
            return null;
        }
        catch (XPathExpressionException ex) {
            logger.debug("getEADNode(): Invalid XPath", ex);
            return null;
        }
        catch (SAXException | ParserConfigurationException e) {
            return null;
        }
    }
}
