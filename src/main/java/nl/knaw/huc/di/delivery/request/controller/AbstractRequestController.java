package nl.knaw.huc.di.delivery.request.controller;

import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import nl.knaw.huc.di.delivery.services.UrlDecoderService;
import nl.knaw.huc.di.delivery.util.ErrorHandlingController;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.config.ResourceNotFoundException;
import nl.knaw.huc.di.delivery.api.NoSuchPidException;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.record.service.RecordService;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.request.entity.HoldingRequest;
import nl.knaw.huc.di.delivery.request.entity.Request;
import nl.knaw.huc.di.delivery.request.service.GeneralRequestService;
import nl.knaw.huc.di.delivery.request.util.BulkActionIds;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import jakarta.persistence.criteria.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractRequestController extends ErrorHandlingController {
    private static final DateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final DeliveryProperties deliveryProperties;
    private final MessageSource messageSource;
    private final GeneralRequestService generalRequestService;
    private final RecordService recordService;
    private final UrlDecoderService urlDecoderService;


    public AbstractRequestController(DeliveryProperties deliveryProperties, MessageSource messageSource, GeneralRequestService generalRequestService, RecordService recordService, UrlDecoderService urlDecoderService) {
        this.deliveryProperties = deliveryProperties;
        this.messageSource = messageSource;
        this.generalRequestService = generalRequestService;
        this.recordService = recordService;
        this.urlDecoderService = urlDecoderService;
    }

    /**
     * Translates the path of a URI to a list of holdings.
     *
     * @param path The path containing the holdings.
     * @return A list of holdings.
     */
    protected List<Holding> uriPathToHoldings(String path) {
        try {
            List<Holding> holdings = new ArrayList<>();
            String[] tuples = urlDecoderService.getPidsFromURL(path);
            for (String tuple : tuples) {
                String[] elements = tuple.split(Pattern.quote(deliveryProperties.getHoldingSeparator()));
                Record r = recordService.getRecordByPidAndCreate(elements[0]);

                for (int i = 1; i < Math.max(2, elements.length); i++) {
                    boolean has = false;
                    for (Holding h : r.getHoldings()) {
                        if ((elements.length == 1) || h.getSignature().equals(elements[i])) {
                            holdings.add(h);
                            has = true;
                        }
                    }
                    if (!has) {
                        return null;
                    }
                }
            }
            return holdings;
        }
        catch (NoSuchPidException e) {
            return null;
        }
    }

    /**
     * Checks the holdings of a request.
     *
     * @param model   The model to add errors to.
     * @param request The Request with holdings to check.
     * @return Whether no errors were found.
     */
    protected boolean hasNoAvailableHoldings(Model model, Request request) {
        List<? extends HoldingRequest> holdingRequests = request.getHoldingRequests();
        if (holdingRequests == null) {
            model.addAttribute("error", "availability");
            return true;
        }

        for (HoldingRequest holdingRequest : holdingRequests) {
            Holding h = holdingRequest.getHolding();
            if (h == null) {
                throw new ResourceNotFoundException();
            }
            if (h.getUsageRestriction() == Holding.UsageRestriction.CLOSED) {
                model.addAttribute("error", "restricted");
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a single date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getDateFilter(Map<String, String[]> p) {
        Date date = null;
        if (p.containsKey("date")) {
            try {
                date = API_DATE_FORMAT.parse(p.get("date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid date: " + p.get("date")[0]);
            }
        }
        return date;
    }

    /**
     * Returns a 'from' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getFromDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsFrom = p.containsKey("from_date") && !p.get("from_date")[0].trim().isEmpty();
        if (containsFrom) {
            try {
                date = API_DATE_FORMAT.parse(p.get("from_date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid from_date: " + p.get("from_date")[0]);
            }
        }
        return date;
    }

    /**
     * Returns a 'to' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getToDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsTo = p.containsKey("to_date") && !p.get("to_date")[0].trim().isEmpty();
        if (containsTo) {
            try {
                date = API_DATE_FORMAT.parse(p.get("to_date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid to_date: " + p.get("to_date")[0]);
            }
        }
        return date;
    }

    /**
     * Parse the page filter into a first result integer.
     *
     * @param p The parameter map to search the given filter value in.
     * @return The first result to show.
     */
    @Deprecated
    protected int getFirstResult(Map<String, String[]> p) {
        int maxResults = getMaxResults(p);
        int page = 0;
        if (p.containsKey("page")) {
            try {
                page = Math.max(0, Integer.parseInt(p.get("page")[0]) - 1);
            }
            catch (NumberFormatException ex) {
                throw new InvalidRequestException("Invalid page number: " + p.get("page")[0]);
            }
        }
        return maxResults * page;
    }

    /**
     * Parse the page length filter into a max results integer.
     *
     * @param p The parameter map to search the given filter value in.
     * @return The length of the page, max results (defaults to the length in the config,
     * can not exceed the maximum length in the config).
     */
    protected int getMaxResults(Map<String, String[]> p) {
        int maxResults = deliveryProperties.getRequestPageLen();
        if (p.containsKey("page_len")) {
            try {
                maxResults = Math.max(0, Math.min(Integer.parseInt(p.get("page_len")[0]),
                        deliveryProperties.getRequestMaxPageLen()));
            }
            catch (NumberFormatException ex) {
                throw new InvalidRequestException("Invalid page length: " + p.get("page_len")[0]);
            }
        }
        return maxResults;
    }

    /**
     * Search for holdings and remove the holdings already specified in the given request.
     *
     * @param request         The new request being created.
     * @param searchTitle     The title to search for.
     * @param searchSignature The signature to search for.
     * @param pageable        Pager
     * @return A list of matching holdings not already specified in the given request.
     */
    protected Page<Holding> searchMassCreate(Request request, String searchTitle, String searchSignature, Pageable pageable) {
        if ((searchTitle == null) && (searchSignature == null))
            return Page.empty();

        CriteriaBuilder cb = recordService.getRecordCriteriaBuilder();
        CriteriaQuery<Holding> cq = cb.createQuery(Holding.class);

        Root<Holding> hRoot = cq.from(Holding.class);
        cq.select(hRoot);

        Join<Holding, Record> rRoot = hRoot.join(Holding.JOIN_RECORD);
        Join<Record, ExternalRecordInfo> eRoot = rRoot.join("externalInfo");

        // Separate all keywords, also remove duplicates spaces so the empty string is not being searched for.
        String[] lowSearchTitle = (searchTitle != null)
                ? searchTitle.toLowerCase().replaceAll("\\s+", " ").split(" ")
                : new String[0];
        Expression<Boolean> titleWhere = null;
        for (String s : lowSearchTitle) {
            Expression<Boolean> titleSearch =
                    cb.like(cb.lower(eRoot.get(ExternalRecordInfo.TITLE)), "%" + s + "%");
            titleWhere = (titleWhere == null) ? titleSearch : cb.and(titleWhere, titleSearch);
        }

        String[] lowSearchSignature = (searchSignature != null)
                ? searchSignature.toLowerCase().replaceAll("\\s+", " ").split(" ")
                : new String[0];
        Expression<Boolean> sigWhere = null;
        for (String s : lowSearchSignature) {
            Expression<Boolean> sigSearch = cb.like(cb.lower(hRoot.get(Holding.SIGNATURE)), "%" + s + "%");
            sigWhere = sigWhere == null ? sigSearch : cb.and(sigWhere, sigSearch);
        }

        Expression<Boolean> where;
        if (sigWhere == null) {
            where = titleWhere;
        }
        else if (titleWhere == null) {
            where = sigWhere;
        }
        else {
            where = cb.and(titleWhere, sigWhere);
        }

        // Exclude already included holdings
        if ((request != null) && (request.getHoldingRequests() != null)) {
            for (HoldingRequest hr : request.getHoldingRequests()) {
                where = cb.and(where, cb.notEqual(hRoot.get("id"), hr.getHolding().getId()));
            }
        }

        cq.where(where);
        cq.distinct(true);

        Page<Holding> holdings = recordService.findAll(cq, pageable);

        // Update the external data of the records, if necessary
        Set<Record> r = new HashSet<>();
        for (Holding holding : holdings) {
            Record record = holding.getRecord();
            if (!r.contains(record)) {
                recordService.updateExternalInfo(record, false);
                recordService.saveRecord(record);
                r.add(record);
            }
        }

        return holdings;
    }

    /**
     * Extracts the holdings from a collection of holding requests.
     *
     * @param holdingRequests A collection of holding requests.
     * @return A set of holdings.
     */
    protected Set<Holding> getHoldings(Collection<? extends HoldingRequest> holdingRequests) {
        Set<Holding> holdings = new HashSet<>();
        for (HoldingRequest hr : holdingRequests) {
            holdings.add(hr.getHolding());
        }
        return holdings;
    }

    /**
     * Creates a map with the requests for which the given holdings are active.
     *
     * @param holdings The holdings.
     * @return A map with the requests for which the given holdings are active.
     */
    protected Map<String, Request> getHoldingActiveRequests(Collection<Holding> holdings) {
        Map<String, Request> holdingActiveRequests = new HashMap<>();
        for (Holding holding : holdings) {
            if (!holdingActiveRequests.containsKey(holding.toString())) {
                Request request = generalRequestService.getActiveFor(holding);
                if (request != null)
                    holdingActiveRequests.put(holding.toString(), request);
            }
        }
        return holdingActiveRequests;
    }

    /**
     * From a list of request id and holding id pairs, extract the ids.
     *
     * @param bulk A list of request id and holding id pairs.
     * @return The ids.
     */
    protected Set<BulkActionIds> getIdsFromBulk(List<String> bulk) {
        Set<BulkActionIds> bulkActionIds = new HashSet<>();
        if (bulk != null) {
            for (String bulkIds : bulk) {
                String[] ids = bulkIds.split(":");
                if (ids.length == 2)
                    bulkActionIds.add(new BulkActionIds(Integer.parseInt(ids[0]), Integer.parseInt(ids[1])));
            }
        }
        return bulkActionIds;
    }

    /**
     * From a list of request id and holding id pairs, extract the request ids.
     *
     * @param bulk A set of request ids.
     * @return The request ids.
     */
    protected Set<Integer> getRequestIdsFromBulk(List<String> bulk) {
        Set<Integer> requestIds = new HashSet<>();
        for (BulkActionIds bulkActionIds : getIdsFromBulk(bulk))
            requestIds.add(bulkActionIds.getRequestId());
        return requestIds;
    }

    /**
     * Returns the request as a string.
     *
     * @param request The Request.
     * @return The request as a string.
     */
    protected String getRequestAsString(Request request) {
        if (request instanceof Reservation) {
            return messageSource.getMessage("reservation.id", new Object[]{}, LocaleContextHolder.getLocale()) +
                    " " + ((Reservation) request).getId();
        }
        if (request instanceof Reproduction) {
            return messageSource.getMessage("reproduction.id", new Object[]{}, LocaleContextHolder.getLocale()) +
                    " " + ((Reproduction) request).getId();
        }
        return null;
    }
}
