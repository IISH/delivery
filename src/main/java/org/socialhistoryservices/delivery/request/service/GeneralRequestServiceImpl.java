package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Represents the service of the request package.
 */
@Service
@Transactional
public class GeneralRequestServiceImpl implements GeneralRequestService {
    @Autowired
    private Set<RequestService> requests;

    /**
     * Mark a request, bumping it to the next status.
     *
     * @param r Request to change status for.
     */
    public void markRequest(Request r) {
        for (RequestService requestService : requests)
            requestService.markRequest(r);
    }

    /**
     * Updates the status of a holding.
     *
     * @param holding       The holding.
     * @param status        The new status.
     */
    public void updateHoldingStatus(Holding holding, Holding.Status status) {
        holding.setStatus(status);
    }

    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @return The active request, null if none exist.
     */
    public Request getActiveFor(Holding holding) {
        Request activeRequest = null;
        for (RequestService requestService : requests) {
            Request request = requestService.getActiveFor(holding);
            // The request with the earliest creation date is always the actual active request
            if ((request != null) &&
                    ((activeRequest == null) || activeRequest.getCreationDate().after(request.getCreationDate()))) {
                activeRequest = request;
            }
        }
        return activeRequest;
    }
}
