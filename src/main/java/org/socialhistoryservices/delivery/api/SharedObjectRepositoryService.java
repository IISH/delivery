package org.socialhistoryservices.delivery.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the Shared Object Repository (SOR) service.
 */
public class SharedObjectRepositoryService {
    private static final Log LOGGER = LogFactory.getLog(SharedObjectRepositoryService.class);
    private static final Pattern HANDLE_PID_PATTERN = Pattern.compile("^http://hdl.handle.net/10622/(.*?)\\?locatt=.*$");

    private static DocumentBuilder documentBuilder;

    static {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            documentBuilder = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String url;

    public SharedObjectRepositoryService(String url) {
        this.url = url;
    }

    /**
     * Find out if the given PID has metadata in the SOR, for both the master and the first derivative.
     *
     * @param pid The pid.
     * @return The SOR has metadata, if found.
     */
    public SorMetadata getMetadataForPid(String pid) {
        try {
            URL req = new URL(url + "/metadata/" + pid + "?accept=text/xml&format=xml");

            LOGGER.debug(String.format("hasMetadata(): Querying SOR API: %s", req.toString()));
            HttpURLConnection conn = (HttpURLConnection) req.openConnection();
            Document document = documentBuilder.parse(conn.getInputStream());

            return getMetadataFromDocument(document, pid);
        }
        catch (IOException ioe) {
            LOGGER.error("getMetadataForPid(): SOR API connection failed", ioe);
            return null;
        }
        catch (SAXException saxe) {
            LOGGER.debug("getMetadataForPid(): Could not parse received metadata", saxe);
            return null;
        }
    }

    /**
     * Parses the document to a SorMetadata object.
     *
     * @param document The document.
     * @param pid      The pid of the item of which we request metadata.
     * @return The metadata from the SOR.
     */
    private SorMetadata getMetadataFromDocument(Document document, String pid) {
        // See if there is an element with the PID and make sure it matches the PID we're requesting
        Node pidNode = getElement(document.getElementsByTagName("pid"));
        if (!pidNode.getTextContent().equals(pid))
            return null;

        String contentTypeMaster = null, contentTypeLevel1 = null;
        Map<String, String> contentMaster = null, contentLevel1 = null;

        // See if the required level exists
        for (String levelTagName : new String[]{"master", "level1"}) {
            Element levelElement = getElement(document.getElementsByTagName(levelTagName));
            if (levelElement != null) {
                // Obtain the various metadata
                Element contentTypeElement = getElement(levelElement.getElementsByTagName("contentType"));
                Element contentElement = getElement(levelElement.getElementsByTagName("content"));

                String contentType = null;
                if (contentTypeElement != null) {
                    contentType = contentTypeElement.getTextContent();
                }

                Map<String, String> contentAttributes = new HashMap<>();
                if ((contentElement != null) && contentElement.hasAttributes()) {
                    NamedNodeMap contentElementAttributes = contentElement.getAttributes();
                    for (int i = 0; i < contentElementAttributes.getLength(); i++) {
                        Node contentElementAttribute = contentElementAttributes.item(i);
                        contentAttributes.put(
                                contentElementAttribute.getNodeName(),
                                contentElementAttribute.getNodeValue()
                        );
                    }
                }

                if (levelTagName.equals("master")) {
                    contentTypeMaster = contentType;
                    contentMaster = contentAttributes;
                }
                else {
                    contentTypeLevel1 = contentType;
                    contentLevel1 = contentAttributes;
                }
            }
        }

        // Determine METS
        boolean isMETS = false;
        Map<String, List<String>> filePids = null;
        if ((contentTypeMaster != null) &&
                (contentTypeMaster.equalsIgnoreCase("application/xml")
                        || contentTypeMaster.equalsIgnoreCase("text/xml"))) {
            filePids = getFilesMETS(pid);
            isMETS = (filePids != null);
        }

        return new SorMetadata(contentTypeMaster, contentTypeLevel1, contentMaster, contentLevel1, isMETS, filePids);
    }

    /**
     * Obtains the METS based on the PID and obtains the files and their PIDs from the document.
     *
     * @param pid The PID of a METS document.
     * @return A map with the uses and their file PIDS.
     */
    private Map<String, List<String>> getFilesMETS(String pid) {
        try {
            URL req = new URL(url + "/file/master/" + pid);

            LOGGER.debug(String.format("getFilesMETS(): Obtain METS document: %s", req.toString()));
            HttpURLConnection conn = (HttpURLConnection) req.openConnection();
            Document document = documentBuilder.parse(conn.getInputStream());

            return getFilesMETSFromDocument(document);
        }
        catch (IOException ioe) {
            LOGGER.error("getFilesMETS(): Could not obtain METS document", ioe);
            return null;
        }
        catch (SAXException saxe) {
            LOGGER.debug("getFilesMETS(): Could not parse received METS", saxe);
            return null;
        }
    }

    /**
     * Parses the METS document for the files and their PIDs.
     *
     * @param document The METS document.
     * @return A map with the uses and their file PIDs.
     */
    private static Map<String, List<String>> getFilesMETSFromDocument(Document document) {
        Map<String, List<String>> filePids = new HashMap<>();

        Map<String, Set<String>> fptrsPerGroup = gettFptrsPerGroup(document);
        Map<Integer, Set<String>> fptrsOrdered = getFptrsOrdered(document);
        for (String fileGrp : fptrsPerGroup.keySet()) {
            List<String> files = new ArrayList<>();

            for (Integer order : fptrsOrdered.keySet()) {
                Set<String> groupFptrs = fptrsPerGroup.get(fileGrp);
                Set<String> orderedFptrs = fptrsOrdered.get(order);

                for (String fptr : orderedFptrs) {
                    if (groupFptrs.contains(fptr))
                        files.add(getFilePidFromFptr(document, fptr));
                }
            }

            if (!files.isEmpty())
                filePids.put(fileGrp, files);
        }

        return filePids;
    }

    /**
     * Obtains all file pointers per group in the METS.
     *
     * @param document The METS document.
     * @return A map with all file pointers per group.
     */
    private static Map<String, Set<String>> gettFptrsPerGroup(Document document) {
        Map<String, Set<String>> fptrsPerGroup = new TreeMap<>();
        for (Element fileGrpElement : getElements(document.getElementsByTagName("fileGrp"))) {
            Set<String> fptrs = new HashSet<>();
            for (Element fptrElement : getElements(fileGrpElement.getElementsByTagName("file")))
                fptrs.add(fptrElement.getAttribute("ID"));
            String fileGrpId = fileGrpElement.getAttribute("USE");
            fptrsPerGroup.put(fileGrpId, fptrs);
        }
        return fptrsPerGroup;
    }

    /**
     * Obtains all file pointers per sequence order in the METS.
     *
     * @param document The METS document.
     * @return A map with all file pointers per sequence order.
     */
    private static Map<Integer, Set<String>> getFptrsOrdered(Document document) {
        Map<Integer, Set<String>> fptrsPerPage = new TreeMap<>();
        for (Element structMapElement : getElements(document.getElementsByTagName("structMap"))) {
            if (structMapElement.getAttribute("TYPE").equals("physical")) {
                for (Element divElement : getElements(structMapElement.getElementsByTagName("div"))) {
                    if (divElement.getAttribute("TYPE").equals("page")) {
                        Set<String> fptrs = new HashSet<>();
                        for (Element fptrElement : getElements(divElement.getElementsByTagName("fptr")))
                            fptrs.add(fptrElement.getAttribute("FILEID"));
                        fptrsPerPage.put(Integer.parseInt(divElement.getAttribute("ORDER")), fptrs);
                    }
                }
            }
        }
        return fptrsPerPage;
    }

    /**
     * Returns the PID for a file with the given file pointer id.
     *
     * @param document The METS document.
     * @param id       The file pointer id.
     * @return The PID.
     */
    private static String getFilePidFromFptr(Document document, String id) {
        for (Element fileElement : getElements(document.getElementsByTagName("file"))) {
            if (fileElement.getAttribute("ID").equals(id)) {
                Element fLocatElement = getElement(fileElement.getElementsByTagName("FLocat"));
                if (fLocatElement != null) {
                    String url = fLocatElement.getAttribute("xlink:href");
                    Matcher matcher = HANDLE_PID_PATTERN.matcher(url);
                    if (matcher.find())
                        return "10622/" + matcher.group(1);
                }
            }
        }
        return null;
    }

    /**
     * Get an element from the node list.
     *
     * @param nodeList The node list.
     * @return The first element, if it exists.
     */
    private static Element getElement(NodeList nodeList) {
        List<Element> elements = getElements(nodeList);
        if (!elements.isEmpty()) {
            return elements.get(0);
        }
        return null;
    }

    /**
     * Obtains a list of elements from a node list.
     *
     * @param nodeList The node list.
     * @return The elements.
     */
    private static List<Element> getElements(NodeList nodeList) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nodeList.item(i);
                elements.add(element);
            }
        }
        return elements;
    }
}
