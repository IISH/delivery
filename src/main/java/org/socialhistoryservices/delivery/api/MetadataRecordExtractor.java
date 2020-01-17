package org.socialhistoryservices.delivery.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.socialhistoryservices.delivery.record.entity.ArchiveHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;

public interface MetadataRecordExtractor {
    static String stripToSize(String string, int size) {
        if (string.length() > size) {
            string = string.substring(0, size);
            string = string.trim();
        }
        return string;
    }

    /**
     * Returns the PID of the record.
     *
     * @return The PID.
     */
    String getPid();

    /**
     * Extracts the metadata of the record.
     *
     * @return The metadata of the record, if found.
     */
    ExternalRecordInfo getRecordMetadata();

    /**
     * Get a map of holding signatures associated with this records,
     * linking to additional holding info provided by the API.
     *
     * @return A map of found (signature, holding info) tuples, or an empty map if none were found.
     */
    Map<String, ExternalHoldingInfo> getHoldingMetadata();

    /**
     * Obtains archive holding info of a record.
     *
     * @return A list with the archive metadata of the record, if found.
     */
    List<ArchiveHoldingInfo> getArchiveHoldingInfo();

    /**
     * Obtains metadata record extractors for all container siblings of the current record.
     * These records do not only share the same parent record, but also share a common container.
     *
     * @return A set of metadata record extractors for the container siblings.
     */
    Set<MetadataRecordExtractor> getRecordExtractorsForContainerSiblings();
}
