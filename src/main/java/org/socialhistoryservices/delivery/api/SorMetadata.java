package org.socialhistoryservices.delivery.api;

import java.util.List;
import java.util.Map;

/**
 * Maps some of the data obtained from the metadata API from the Shared Object Repository.
 */
public class SorMetadata {
    private static final int TIFF_MASTER_DPI = 300;

    private final String contentTypeMaster;
    private final String contentTypeLevel1;

    private final Map<String, String> contentMaster;
    private final Map<String, String> contentLevel1;

    private final boolean isMETS;
    private final Map<String, List<String>> filePids;

    public SorMetadata(String contentTypeMaster, String contentTypeLevel1, Map<String, String> contentMaster,
                       Map<String, String> contentLevel1, boolean isMETS, Map<String, List<String>> filePids) {
        this.contentTypeMaster = contentTypeMaster;
        this.contentTypeLevel1 = contentTypeLevel1;
        this.contentMaster = contentMaster;
        this.contentLevel1 = contentLevel1;
        this.isMETS = isMETS;
        this.filePids = filePids;
    }

    /**
     * The content type of the file.
     *
     * @param level The file with the given level.
     * @return The content type of the file.
     */
    public String getContentType(String level) {
        return (level.equalsIgnoreCase("master")) ? getContentTypeMaster() : getContentTypeLevel1();
    }

    /**
     * The content type of the master file.
     *
     * @return The content type of the master file.
     */
    public String getContentTypeMaster() {
        return contentTypeMaster;
    }

    /**
     * The content type of the level1 file.
     *
     * @return The content type of the level1 file.
     */
    public String getContentTypeLevel1() {
        return contentTypeLevel1;
    }

    /**
     * The master content metadata.
     *
     * @param level The file with the given level.
     * @return The master content metadata.
     */
    public Map<String, String> getContent(String level) {
        return (level.equalsIgnoreCase("master")) ? getContentMaster() : getContentLevel1();
    }

    /**
     * The master content metadata.
     *
     * @return The master content metadata.
     */
    public Map<String, String> getContentMaster() {
        return contentMaster;
    }

    /**
     * The level1 content metadata.
     *
     * @return The level1 content metadata.
     */
    public Map<String, String> getContentLevel1() {
        return contentLevel1;
    }

    /**
     * Whether this is a METS document.
     *
     * @return Whether this is a METS document.
     */
    public boolean isMETS() {
        return isMETS;
    }

    /**
     * The use levels and the PIDS of the files contained in the METS document.
     *
     * @return The use levels and the PIDS of the files contained in the METS document.
     */
    public Map<String, List<String>> getFilePids() {
        return filePids;
    }

    /**
     * Try to determine if this master file is a TIFF with the valid DPI.
     *
     * @return True if we can determine that the master file is a TIFF with the valid DPI.
     */
    public boolean isTiff() {
        try {
            if ((contentTypeMaster != null) && contentTypeMaster.equals("image/tiff")) {
                if (contentMaster.containsKey("x-resolution")) {
                    String xResolution = contentMaster.get("x-resolution");

                    // Try to obtain the number such that we can determine the DPI value
                    xResolution = xResolution.replaceAll("([^\\d]*)([\\d]*)(.*)", "$2");
                    int dpi = Integer.parseInt(xResolution);

                    return (dpi >= TIFF_MASTER_DPI);
                }
            }
            return false;
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }
}
