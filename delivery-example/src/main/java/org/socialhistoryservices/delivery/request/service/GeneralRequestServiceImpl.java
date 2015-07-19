package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.service.OnHoldException;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
     * Updates the status of a holding.
     *
     * @param holding The holding.
     * @param status  The new status.
     */
    public void updateHoldingStatus(Holding holding, Holding.Status status) {
        holding.setStatus(status);
        checkRequestHoldingsOnHold(holding);
        sentHoldingStatusUpdateEvent(holding);
    }

    /**
     * Go over the holdings of both requests to find differences in holding status.
     * For the differences, check holdings that are on hold and sent the status updated event.
     *
     * @param newRequest The new request.
     * @param oldRequest The old request.
     */
    public void updateHoldingStatusAfterMerge(Request newRequest, Request oldRequest) {
        List<Holding> newRequestHoldings = newRequest.getHoldings();
        List<Holding> oldRequestHoldings = oldRequest.getHoldings();

        for (Holding newHolding : newRequestHoldings) {
            boolean has = false;

            // First try to find if the holding was also present in the old request and had a status update
            for (Holding oldHolding : oldRequestHoldings) {
                if (newHolding.getSignature().equals(oldHolding.getSignature())) {
                    has = true;
                    if (newHolding.getStatus() != oldHolding.getStatus())
                        updateHoldingStatus(newHolding, newHolding.getStatus());
                }
            }

            // If not found, it is likely the status has changed to 'reserved'
            if (!has)
                updateHoldingStatus(newHolding, newHolding.getStatus());
        }

        // For all holdings in the old request, it is likely the status has changed to 'available'
        for (Holding oldHolding : oldRequestHoldings) {
            boolean has = false;

            for (Holding newHolding : newRequestHoldings) {
                if (newHolding.getSignature().equals(oldHolding.getSignature()))
                    has = true;
            }

            if (!has)
                updateHoldingStatus(oldHolding, oldHolding.getStatus());
        }
    }

    /**
     * Mark a specific item in a request on hold.
     *
     * @param holding Holding in question.
     * @return The request that was updated.
     * @throws OnHoldException If the holding was already placed on hold for a request.
     */
    public Request markItemOnHold(Holding holding) throws OnHoldException {
        Request request = getOnHoldFor(holding);
        if (request != null)
            throw new OnHoldException(request, holding);

        for (HoldingRequest hr : request.getHoldingRequests()) {
            Holding h = hr.getHolding();
            if ((holding.getId() == h.getId()) && (holding.getStatus() == Holding.Status.IN_USE)) {
                hr.setOnHold(true);
            }
        }

        return request;
    }

    /**
     * Mark a specific item in a request as active after being on hold.
     *
     * @param holding Holding in question.
     * @return The request that was updated.
     * @throws OnHoldException If no holding was placed on hold for a request.
     */
    public Request markItemActive(Holding holding) throws OnHoldException {
        Request request = getOnHoldFor(holding);
        if (request == null)
            throw new OnHoldException(holding);

        for (HoldingRequest hr : request.getHoldingRequests()) {
            Holding h = hr.getHolding();
            if (holding.getId() == h.getId()) {
                hr.setOnHold(false);
                h.setStatus(Holding.Status.IN_USE);
            }
        }

        return request;
    }

    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @return The active request, null if none exist.
     */
    public Request getActiveFor(Holding holding) {
        return getActiveFor(holding, 1);
    }

    /**
     * Get a request relating to a specific Holding which is placed on hold.
     *
     * @param holding Holding to find a request for.
     * @return The request which is on hold, null if none exist.
     */
    public Request getOnHoldFor(Holding holding) {
        return getActiveFor(holding, -1);
    }

    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @param getAll  Whether to return all active requests (0)
     *                or only those that are on hold (< 0) or those that are NOT on hold (> 0).
     * @return The active request, null if none exist.
     */
    public Request getActiveFor(Holding holding, int getAll) {
        for (RequestService requestService : requests) {
            Request request = requestService.getActiveFor(holding, getAll);
            if (request != null)
                return request;
        }
        return null;
    }

    /**
     * Lets all the request services know that the status of one of the holdings is updated.
     *
     * @param holding The holding.
     */
    private void sentHoldingStatusUpdateEvent(Holding holding) {
        for (RequestService requestService : requests) {
            requestService.onHoldingStatusUpdate(holding);
        }
    }

    /**
     * If the holding is now available or returned,
     * make sure that other requests with this holding on hold are now no longer on hold.
     *
     * @param holding The holding.
     */
    private void checkRequestHoldingsOnHold(Holding holding) {
        try {
            Holding.Status newStatus = holding.getStatus();
            if ((newStatus == Holding.Status.AVAILABLE) || (newStatus == Holding.Status.RETURNED)) {
                markItemActive(holding);
            }
        } catch (OnHoldException e) {
            // No problem, we don't expect a holding to be on hold all the time
        }
    }
}
