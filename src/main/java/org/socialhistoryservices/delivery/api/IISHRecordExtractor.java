package org.socialhistoryservices.delivery.api;

import org.socialhistoryservices.delivery.record.entity.ExternalHoldingInfo;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Map;

interface IISHRecordExtractor {

    /**
     * Parses a node to metadata of a record.
     *
     * @param node The node to parse.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    ExternalRecordInfo getRecordMetadata(Node node) throws NoSuchPidException;

    /**
     * Parses a node to metadata of a record.
     *
     * @param node The node to parse.
     * @param item The item.
     * @return The metadata of the record, if found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    ExternalRecordInfo getRecordMetadata(Node node, String item) throws NoSuchPidException;

    /**
     * Get a map of holding signatures associated with this node,
     * linking to additional holding info provided by the API.
     *
     * @param node The node to parse.
     * @return A map of found (signature,holding info) tuples, or an empty map if none were found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    Map<String, ExternalHoldingInfo> getHoldingMetadata(Node node) throws NoSuchPidException;

    /**
     * Get a map of holding signatures associated with this node,
     * linking to additional holding info provided by the API.
     *
     * @param node The node to parse.
     * @param item The item.
     * @return A map of found (signature,holding info) tuples, or an empty map if none were found.
     * @throws NoSuchPidException Thrown when the PID does not exist.
     */
    Map<String, ExternalHoldingInfo> getHoldingMetadata(Node node, String item) throws NoSuchPidException;

    String getUnitIdsFromContainer(Node node, String pid, String container, boolean includeCurrentContainer) throws NoSuchPidException;
}
