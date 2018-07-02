package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.Request;

/**
 * Represents the service of the request package to be used by the implementing services.
 */
public interface RequestService {

    /**
     * Mark a request, bumping it to the next status.
     *
     * @param r Request to change status for.
     */
    void markRequest(Request r);

    /**
     * Returns the active request with which this holding is associated.
     *
     * @param holding The Holding to get the active reservation of.
     * @return The active request, or null if no active request exists.
     */
    Request getActiveFor(Holding holding);
}
