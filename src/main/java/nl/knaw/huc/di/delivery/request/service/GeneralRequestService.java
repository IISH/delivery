package nl.knaw.huc.di.delivery.request.service;

import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.request.entity.Request;

/**
 * Interface representing the service of the RequestService package.
 */
public interface GeneralRequestService {
    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @return The active request, null if none exist.
     */
    Request getActiveFor(Holding holding);

    void addRequest(RequestService requestService);
}
