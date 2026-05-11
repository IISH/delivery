package nl.knaw.huc.di.delivery.request.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.request.entity.Request;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the service of the request package.
 */
@Service
public class GeneralRequestServiceImpl implements GeneralRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRequestServiceImpl.class);

    private final Set<RequestService> requestServices;

    public GeneralRequestServiceImpl() {
        this.requestServices = new HashSet<>();
    }

    /**
     * Get an active request relating to a specific Holding.
     *
     * @param holding Holding to find a request for.
     * @return The active request, null if none exist.
     */
    public Request getActiveFor(Holding holding) {
        Request activeRequest = null;
        for (RequestService requestService : requestServices) {
            Request request = requestService.getActiveFor(holding);
            // The request with the earliest creation date is always the actual active request
            if ((request != null) &&
                    ((activeRequest == null) || activeRequest.getCreationDate().after(request.getCreationDate()))) {
                activeRequest = request;
            }
        }
        return activeRequest;
    }

    /**
     * We cannot autowire the list of request services due to the subsequent dependency cycle.
     * Hence we post initialze the set after the dependency injection took place.
     *
     * @param requestService The serviceRequest object
     */
    @Override
    public void addRequest(RequestService requestService) {
        requestServices.add(requestService);
        LOGGER.info("Added request service " + requestService);
    }
}
