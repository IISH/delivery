package org.socialhistoryservices.delivery.request.controller;

import org.socialhistoryservices.delivery.ErrorHandlingController;
import org.socialhistoryservices.delivery.InvalidRequestException;
import org.socialhistoryservices.delivery.ResourceNotFoundException;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.service.PermissionService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.record.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbstractRequestController extends ErrorHandlingController {
    private static final DateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
        Map<String, Holding.Status> data = new HashMap<String, Holding.Status>();
        data.put("AVAILABLE", Holding.Status.AVAILABLE);
        data.put("RESERVED", Holding.Status.RESERVED);
        data.put("IN_USE", Holding.Status.IN_USE);
        data.put("RETURNED", Holding.Status.RETURNED);
        return data;
    }

    /**
     * Translates the path of a URI to a list of holdings.
     *
     * @param path            The path containing the holdings.
     * @param mustBeAvailable If only a PID is given, find specifically an available holding?
     * @return A list of holdings.
     */
    protected List<Holding> uriPathToHoldings(String path, boolean mustBeAvailable) {
        List<Holding> holdings = new ArrayList<Holding>();
        String[] tuples = getPidsFromURL(path);
        for (String tuple : tuples) {
            String[] elements = tuple.split(properties.getProperty("prop_holdingSeparator", ":"));
            Record r = records.getRecordByPid(elements[0]);
            if (r == null) {
                // Try creating the record.
                try {
                    r = records.createRecordByPid(elements[0]);
                    records.addRecord(r);
                } catch (NoSuchPidException e) {
                    return null;
                }
            }
            if (elements.length == 1) {
                Holding h = records.getHoldingForRecord(r, mustBeAvailable);
                if (h == null) {
                    return null;
                }
                holdings.add(h);
            } else {
                for (int i = 1; i < elements.length; i++) {
                    boolean has = false;
                    for (Holding h : r.getHoldings()) {
                        if (h.getSignature().equals(elements[i])) {
                            holdings.add(h);
                            has = true;
                        }
                    }
                    if (!has) {
                        return null;
                    }
                }
            }
        }
        return holdings;
    }

    /**
     * Determine if the given time is between today->open and today->close of the readingroom.
     *
     * @param time The given time.
     * @return Whether the given time is between opening and closing time.
     */
    protected boolean isBetweenOpeningAndClosingTime(Date time) {
        Date open, close;
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setLenient(false);

        try {
            open = format.parse(properties.getProperty("prop_requestAutoPrintStartTime"));
            close = format.parse(properties.getProperty("prop_requestLatestTime"));
        } catch (ParseException e) {
            throw new RuntimeException("Failed parsing necessary open and " +
                    "closing dates for auto printing. Are they in the " +
                    "correct HH:mm format in your properties file?");
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(time);

        int createH = cal.get(Calendar.HOUR_OF_DAY);
        int createM = cal.get(Calendar.MINUTE);
        cal.setTime(open);

        int openH = cal.get(Calendar.HOUR_OF_DAY);
        int openM = cal.get(Calendar.MINUTE);
        cal.setTime(close);

        int closeH = cal.get(Calendar.HOUR_OF_DAY);
        int closeM = cal.get(Calendar.MINUTE);

        boolean afterOpen = createH > openH || (createH == openH && createM >= openM);
        boolean beforeClose = createH < closeH || (createH == closeH && createM < closeM);

        return (afterOpen && beforeClose);
    }

    protected boolean checkHoldings(Model model, Request request) {
        List<? extends HoldingRequest> holdingRequests = request.getHoldingRequests();
        if (holdingRequests == null) {
            model.addAttribute("error", "availability");
            return false;
        }

        // TODO: Only reservations ???
        if (holdingRequests.size() > Integer.parseInt(properties.getProperty("prop_requestMaxItems"))) {
            model.addAttribute("error", "limitItems");
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
     * Checks the permission if applicable (i.e. holdings selected are tied to one or more restricted records).
     *
     * @param model   The model to add errors to.
     * @param perm    The permission, can be null if not applicable.
     * @param request The Request with holdings to check.
     * @return True iff the given permission (can be null) is allowed to reserve the provided holdings.
     */
    protected boolean checkPermissions(Model model, Permission perm, Request request) {
        if (perm == null) {
            perm = new Permission();
        }

        List<? extends HoldingRequest> holdingRequests = request.getHoldingRequests();
        for (HoldingRequest holdingRequest : holdingRequests) {
            Record permRecord = holdingRequest.getHolding().getRecord();
            if (permRecord.getRealRestrictionType() == Record.RestrictionType.RESTRICTED) {
                if (!perm.hasGranted(permRecord)) {
                    model.addAttribute("holding" + request.getClass().getSimpleName() + "s", holdingRequest);
                    return false;
                }
            }
        }
        return true;
    }

    protected void initOverviewModel(Map<String, String[]> p, Model model, PagedListHolder<?> pagedListHolder) {
        // Set the amount of reproductions per page
        pagedListHolder.setPageSize(parsePageLenFilter(p));

        // Set the current page, internal starts at 0, external at 1
        pagedListHolder.setPage(parsePageFilter(p));

        // Add result to model
        model.addAttribute("pageListHolder", pagedListHolder);

        Calendar cal = GregorianCalendar.getInstance();
        model.addAttribute("today", cal.getTime());

        cal.add(Calendar.MONTH, -3);
        model.addAttribute("min3months", cal.getTime());

        cal.add(Calendar.MONTH, 3);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        model.addAttribute("tomorrow", cal.getTime());
    }

    protected Date getDateFilter(Map<String, String[]> p) {
        Date date = null;
        if (p.containsKey("date")) {
            try {
                date = API_DATE_FORMAT.parse(p.get("date")[0]);
            } catch (ParseException ex) {
                throw new InvalidRequestException("Invalid date: " + p.get("date")[0]);
            }
        }
        return date;
    }

    protected Date getFromDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsFrom = p.containsKey("from_date") && !p.get("from_date")[0].trim().equals("");
        if (containsFrom) {
            try {
                date = API_DATE_FORMAT.parse(p.get("from_date")[0]);
            } catch (ParseException ex) {
                throw new InvalidRequestException("Invalid from_date: " + p.get("from_date")[0]);
            }
        }
        return date;
    }

    protected Date getToDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsTo = p.containsKey("to_date") && !p.get("to_date")[0].trim().equals("");
        if (containsTo) {
            try {
                date = API_DATE_FORMAT.parse(p.get("to_date")[0]);
            } catch (ParseException ex) {
                throw new InvalidRequestException("Invalid to_date: " + p.get("to_date")[0]);
            }
        }
        return date;
    }

    /**
     * Parse the page filter into an integer.
     *
     * @param p The parameter map to search the given filter value in.
     * @return The current page to show, default 0. (external = 1).
     */
    protected int parsePageFilter(Map<String, String[]> p) {
        int page = 0;
        if (p.containsKey("page")) {
            try {
                page = Math.max(0, Integer.parseInt(p.get("page")[0]) - 1);
            } catch (NumberFormatException ex) {
                throw new InvalidRequestException("Invalid page number: " + p.get("page")[0]);
            }
        }
        return page;
    }

    /**
     * Parse the page length filter.
     *
     * @param p The parameter map to search the given filter value in.
     * @return The length of the page (defaults to the length in the config,
     * can not exceed the maximum length in the config).
     */
    protected int parsePageLenFilter(Map<String, String[]> p) {
        int pageLen = Integer.parseInt(properties.getProperty("prop_requestPageLen"));
        if (p.containsKey("page_len")) {
            try {
                pageLen = Math.max(0, Math.min(Integer.parseInt(p.get("page_len")[0]),
                        Integer.parseInt(properties.getProperty("prop_requestMaxPageLen"))));
            } catch (NumberFormatException ex) {
                throw new InvalidRequestException("Invalid page length: " + p.get("page_len")[0]);
            }
        }
        return pageLen;
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
        if ((searchTitle == null) && (searchSignature == null))
            return new ArrayList<Holding>();

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
                    cb.like(cb.lower(eRoot.<String>get(ExternalRecordInfo_.title)), "%" + s + "%");
            titleWhere = (titleWhere == null) ? titleSearch : cb.and(titleWhere, titleSearch);
        }

        String[] lowSearchSignature = (searchSignature != null)
                ? searchSignature.toLowerCase().replaceAll("\\s+", " ").split(" ")
                : new String[0];
        Expression<Boolean> sigWhere = null;
        for (String s : lowSearchSignature) {
            Expression<Boolean> sigSearch = cb.like(cb.lower(hRoot.<String>get(Holding_.signature)), "%" + s + "%");
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

        return records.listHoldings(cq);
    }
}