package org.socialhistoryservices.delivery.request.controller;

import org.socialhistoryservices.delivery.util.ErrorHandlingController;
import org.socialhistoryservices.delivery.util.InvalidRequestException;
import org.socialhistoryservices.delivery.util.ResourceNotFoundException;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.permission.service.PermissionService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.record.service.RecordService;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.request.service.GeneralRequestService;
import org.socialhistoryservices.delivery.request.util.BulkActionIds;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.w3c.dom.Node;

import javax.persistence.criteria.*;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractRequestController extends ErrorHandlingController {
    private static final DateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    protected MessageSource messageSource;

    @Autowired
    protected GeneralRequestService requests;

    @Autowired
    protected PermissionService permissions;

    @Autowired
    protected RecordService records;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);

        // This is needed for passing an holding ID.
        binder.registerCustomEditor(Holding.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                Holding h = records.getHoldingById(Integer.parseInt(text));
                setValue(h);
            }
        });
    }

    /**
     * Map representation of status types of reservations for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("holding_status_types")
    public Map<String, Holding.Status> holdingStatusTypes() {
        Map<String, Holding.Status> data = new LinkedHashMap<>();
        data.put("AVAILABLE", Holding.Status.AVAILABLE);
        data.put("RESERVED", Holding.Status.RESERVED);
        data.put("IN_USE", Holding.Status.IN_USE);
        data.put("RETURNED", Holding.Status.RETURNED);
        return data;
    }

    /**
     * Map representation of material types for use in views.
     *
     * @return The map {string material type, enum status}.
     */
    @ModelAttribute("material_types")
    public Map<String, ExternalRecordInfo.MaterialType> materialTypes() {
        Map<String, ExternalRecordInfo.MaterialType> data = new LinkedHashMap<>();
        data.put("SERIAL", ExternalRecordInfo.MaterialType.SERIAL);
        data.put("BOOK", ExternalRecordInfo.MaterialType.BOOK);
        data.put("SOUND", ExternalRecordInfo.MaterialType.SOUND);
        data.put("DOCUMENTATION", ExternalRecordInfo.MaterialType.DOCUMENTATION);
        data.put("ARCHIVE", ExternalRecordInfo.MaterialType.ARCHIVE);
        data.put("VISUAL", ExternalRecordInfo.MaterialType.VISUAL);
        data.put("MOVING_VISUAL", ExternalRecordInfo.MaterialType.MOVING_VISUAL);
        data.put("ARTICLE", ExternalRecordInfo.MaterialType.ARTICLE);
        data.put("OTHER", ExternalRecordInfo.MaterialType.OTHER);
        return data;
    }

    /**
     * Translates the path of a URI to a list of holdings.
     *
     * @param path The path containing the holdings.
     * @return A list of holdings.
     */
    protected List<Holding> uriPathToHoldings(String path, boolean doImportOtherUnitIds) {
        List<Holding> holdings = new ArrayList<>();
        String[] tuples = getPidsFromURL(path);
        for (String tuple : tuples) {
            String[] elements = tuple.split(Pattern.quote(deliveryProperties.getHoldingSeparator()));
            Record r = records.getRecordByPid(elements[0]);

            if (r == null) {
                // Try creating the record.
                try {
                    r = records.createRecordByPid(elements[0]);
                    records.addRecord(r);
                }
                catch (NoSuchPidException e) {
                    return null;
                }
            } else {
                if (doImportOtherUnitIds) {
                    if (records.updateExternalInfo(r, false)) {
                        records.saveRecord(r);
                    }
                }
            }

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

    /**
     * Checks the holdings of a request.
     *
     * @param model   The model to add errors to.
     * @param request The Request with holdings to check.
     * @return Whether no errors were found.
     */
    protected boolean checkHoldings(Model model, Request request) {
        List<? extends HoldingRequest> holdingRequests = request.getHoldingRequests();
        if (holdingRequests == null) {
            model.addAttribute("error", "availability");
            return false;
        }

        for (HoldingRequest holdingRequest : holdingRequests) {
            Holding h = holdingRequest.getHolding();
            if (h == null) {
                throw new ResourceNotFoundException();
            }
            if (h.getUsageRestriction() == Holding.UsageRestriction.CLOSED) {
                model.addAttribute("error", "restricted");
                return false;
            }
        }

        return true;
    }

    /**
     * Initilizes the model for use with an overview.
     *
     * @param model The model.
     */
    protected void initOverviewModel(Model model) {
        Calendar cal = GregorianCalendar.getInstance();
        model.addAttribute("today", cal.getTime());

        cal.add(Calendar.MONTH, -3);
        model.addAttribute("min3months", cal.getTime());

        cal.add(Calendar.MONTH, 3);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        model.addAttribute("tomorrow", cal.getTime());
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
        boolean containsFrom = p.containsKey("from_date") && !p.get("from_date")[0].trim().equals("");
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
        boolean containsTo = p.containsKey("to_date") && !p.get("to_date")[0].trim().equals("");
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

    protected List<Holding> getHoldingsWithSpecificStatus(ArrayList<String> pids, Holding.Status holdingStatus) {
        if (pids == null || pids.size() == 0) {
            return null;
        }

        CriteriaBuilder cb = records.getRecordCriteriaBuilder();
        CriteriaQuery<Holding> cq = cb.createQuery(Holding.class);
        Root<Holding> rHolding = cq.from(Holding.class);
        cq.select(rHolding);

        Join<Holding, Record> rRecord = rHolding.join(Holding_.record);

        Expression<Boolean> whereRecordPid = null;

        for (String aPid : pids) {
            whereRecordPid = whereRecordPid == null ? cb.or(cb.equal(rRecord.get(Record_.pid), aPid)) : cb.or(whereRecordPid, cb.equal(rRecord.get(Record_.pid), aPid));
        }

        Expression<Boolean> whereHoldingStatus = cb.and(cb.equal(rHolding.get(Holding_.status), holdingStatus));

        cq.where(cb.and(whereRecordPid, whereHoldingStatus));

        cq.distinct(true);

        return records.listHoldings(cq);
    }

    /**
     * Search for holdings and remove the holdings already specified in the given request.
     *
     * @param request         The new request being created.
     * @param searchTitle     The title to search for.
     * @param searchSignature The signature to search for.
     * @return A list of matching holdings not already specified in the given request.
     */
    protected List<Holding> searchMassCreate(Request request, String searchTitle, String searchSignature) {
        if ((searchTitle == null) && (searchSignature == null)) {
            return new ArrayList<>();
        }

        CriteriaBuilder cb = records.getRecordCriteriaBuilder();
        CriteriaQuery<Holding> cq = cb.createQuery(Holding.class);

        Root<Holding> hRoot = cq.from(Holding.class);
        cq.select(hRoot);

        Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
        Join<Record, ExternalRecordInfo> eRoot = rRoot.join(Record_.externalInfo);

        // Separate all keywords, also remove duplicates spaces so the empty string is not being searched for.
        String[] lowSearchTitle = (searchTitle != null)
                ? searchTitle.toLowerCase().replaceAll("\\s+", " ").split(" ")
                : new String[0];
        Expression<Boolean> titleWhere = null;
        for (String s : lowSearchTitle) {
            Expression<Boolean> titleSearch =
                    cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)), "%" + s + "%");
            titleWhere = (titleWhere == null) ? titleSearch : cb.and(titleWhere, titleSearch);
        }

        String[] lowSearchSignature = (searchSignature != null)
                ? searchSignature.toLowerCase().replaceAll("\\s+", " ").split(" ")
                : new String[0];
        Expression<Boolean> sigWhere = null;
        for (String s : lowSearchSignature) {
            Expression<Boolean> sigSearch = cb.like(cb.lower(hRoot.get(Holding_.signature)), "%" + s + "%");
            sigWhere = sigWhere == null ? sigSearch : cb.and(sigWhere, sigSearch);
        }

        Expression<Boolean> where = null;
        if (sigWhere == null) {
            where = titleWhere;
        } else if (titleWhere == null) {
            where = sigWhere;
        } else {
            where = cb.and(titleWhere, sigWhere);
        }

        // Exclude already included holdings
        if ((request != null) && (request.getHoldingRequests() != null)) {
            for (HoldingRequest hr : request.getHoldingRequests()) {
                where = cb.and(where, cb.notEqual(hRoot.get(Holding_.id), hr.getHolding().getId()));
            }
        }

        cq.where(where);
        // cq.orderBy(cb.asc(eRoot.get(ExternalRecordInfo_.title)));
        cq.distinct(true);

        List<Holding> holdings = records.listHoldings(cq);

        // Update the external data of the records, if necessary
        Set<Record> r = new HashSet<>();
        for (Holding holding : holdings) {
            Record record = holding.getRecord();
            if (!r.contains(record)) {
                records.updateExternalInfo(record, false);
                records.saveRecord(record);
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
                Request request = requests.getActiveFor(holding);
                if (request != null) {
                    holdingActiveRequests.put(holding.toString(), request);
                }
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
    protected List<BulkActionIds> getIdsFromBulk(List<String> bulk) {
        List<BulkActionIds> bulkActionIds = new ArrayList<>();
        for (String bulkIds : bulk) {
            String[] ids = bulkIds.split(":");
            if (ids.length == 2) {
                bulkActionIds.add(new BulkActionIds(Integer.parseInt(ids[0]), Integer.parseInt(ids[1])));
            }
        }
        return bulkActionIds;
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
