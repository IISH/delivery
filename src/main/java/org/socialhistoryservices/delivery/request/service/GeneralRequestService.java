package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.Request;

/**
 * Interface representing the service of the RequestService package.
 */
public interface GeneralRequestService {

    /**
     * Mark a request, bumping it to the next status.
     *
     * @param r Request to change status for.
     */
    void markRequest(Request r);

    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @return The active request, null if none exist.
     */
    Request getActiveFor(Holding holding);
}
