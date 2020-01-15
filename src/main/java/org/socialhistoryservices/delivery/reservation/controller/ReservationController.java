package org.socialhistoryservices.delivery.reservation.controller;

import org.apache.log4j.Logger;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.api.RecordLookupService;
import org.socialhistoryservices.delivery.reservation.service.*;
import org.socialhistoryservices.delivery.util.ResourceNotFoundException;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.service.PermissionService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.record.service.RecordService;
import org.socialhistoryservices.delivery.reproduction.util.DateUtils;
import org.socialhistoryservices.delivery.request.controller.AbstractRequestController;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.request.service.ClosedException;
import org.socialhistoryservices.delivery.request.service.NoHoldingsException;
import org.socialhistoryservices.delivery.request.util.BulkActionIds;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller of the Reservation package, handles all /reservation/* requests.
 */
@Controller
@Transactional
@RequestMapping(value = "/reservation")
public class ReservationController extends AbstractRequestController {

    @Autowired
    private ReservationService reservations;

    @Autowired
    private PermissionService permissions;

    @Autowired
    private ReservationMailer resMailer;

    @Autowired
    private RecordService records;

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private RecordLookupService lookup;

    // {{{ Get API

    /**
     * Fetches one specific reservation.
     *
     * @param id    ID of the reservation to fetch.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id}",
            method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RESERVATION_VIEW')")
    public String getSingle(@PathVariable int id, Model model) {
        Reservation r = reservations.getReservationById(id);
        if (r == null) {
            throw new ResourceNotFoundException();
        }
        model.addAttribute("reservation", r);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(r.getHoldings()));
        return "reservation_get";
    }

    /**
     * Get a list of reservations
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RESERVATION_VIEW')")
    public String get(HttpServletRequest req, Model model) {
        Map<String, String[]> p = req.getParameterMap();
        CriteriaBuilder cb = reservations.getHoldingReservationCriteriaBuilder();

        ReservationSearch search = new ReservationSearch(cb, p);
        CriteriaQuery<HoldingReservation> cq = search.list();
        CriteriaQuery<Long> cqCount = search.count();

        // Fetch result set
        List<HoldingReservation> holdingReservations = reservations.listHoldingReservations(
                cq, getFirstResult(p), getMaxResults(p));
        model.addAttribute("holdingReservations", holdingReservations);

        long holdingReservationsSize = reservations.countHoldingReservations(cqCount);
        model.addAttribute("holdingReservationsSize", holdingReservationsSize);

        // Fetch holding active request information
        Set<Holding> holdings = getHoldings(holdingReservations);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(holdings));

        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, deliveryProperties.getReservationMaxDaysInAdvance());
        model.addAttribute("maxReserveDate", cal.getTime());
        initOverviewModel(model);

        return "reservation_get_list";
    }

    //}}}
    // {{{ Create Form

    /**
     * Show the create form of a reservation (visitors create form).
     *
     * @param req   The HTTP request.
     * @param path  The pid/signature string (URL encoded).
     * @param codes The permission codes to use for restricted records.
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}",
            method = RequestMethod.GET)
    public String showCreateForm(HttpServletRequest req, @PathVariable String path,
                                 @RequestParam(required = false) String[] codes,
                                 Model model) {
        Reservation newRes = new Reservation();
        newRes.setDate(reservations.getFirstValidReservationDate(new Date()));

        newRes.setHoldingReservations(uriPathToHoldingReservations(path));
        return processVisitorReservationCreation(req, newRes, null, codes,
                model, false);
    }

    /**
     * Process the create form of a reservation (visitors create form).
     *
     * @param req    The HTTP request.
     * @param newRes The submitted reservation.
     * @param result The binding result to put errors in.
     * @param codes  The permission codes to use for restricted records.
     * @param model  The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}",
            method = RequestMethod.POST)
    public String processCreateForm(HttpServletRequest req, @ModelAttribute("reservation")
            Reservation newRes,
                                    BindingResult result,
                                    @RequestParam(required = false) String[] codes,
                                    Model model) {
        return processVisitorReservationCreation(req, newRes, result, codes, model, true);
    }

    private String processVisitorReservationCreation(HttpServletRequest req, Reservation newRes,
                                                     BindingResult result,
                                                     String[] codes,
                                                     Model model, boolean commit) {
        if (!checkHoldings(model, newRes)) {
            return "reservation_error";
        }

        // Validate the permission codes and add the found permissions to the reservation
        if (codes != null) {
            for (String code : codes) {
                Permission perm = permissions.getPermissionByCode(code);
                if (checkPermissions(model, perm, newRes)) {
                    newRes.getPermissions().add(perm);
                }
            }
        }

        // If the reservation contains restricted items, go to the choice screen
        List<HoldingReservation> open = findHoldingsOnRestriction(newRes, codes, false);
        List<HoldingReservation> restricted = findHoldingsOnRestriction(newRes, codes, true);
        if (!restricted.isEmpty()) {
            model.addAttribute("reservation", newRes);
            model.addAttribute("holdingReservationsOpen", open);
            model.addAttribute("holdingReservationsRestricted", restricted);
            return "reservation_choice";
        }

        // Removed holdings that are already reserved
        Set<HoldingReservation> hrReserved = new HashSet<>();
        for (HoldingReservation hr : newRes.getHoldingReservations()) {
            if (hr.getHolding().getStatus() != Holding.Status.AVAILABLE) {
                hrReserved.add(hr);
            }
        }
        if (!hrReserved.isEmpty()) {
            newRes.getHoldingReservations().removeAll(hrReserved);
            model.addAttribute("warning", "availability");
        }

        try {
            if (commit) {
                // Make sure a Captcha was entered correctly.
                checkCaptcha(req, result, model);
                reservations.createOrEdit(newRes, null, result);

                if (!result.hasErrors()) {
                    // Mail the confirmation to the visitor.
                    try {
                        resMailer.mailConfirmation(newRes);
                    }
                    catch (MailException e) {
                        log.error("Failed to send email", e);
                        model.addAttribute("error", "mail");
                    }
                    // Automatically print the reservation.
                    autoPrint(newRes);
                    model.addAttribute("reservation", newRes);


                    // set all available holdings in the same container as blocked
                    ArrayList<String> listOfPids = getAllAvailableHoldingsInReservation(newRes);
                    List<Holding> holdingList = getHoldingsWithSpecificStatus(listOfPids, Holding.Status.AVAILABLE);
                    if (holdingList != null) {
                        for (Holding hld : holdingList) {
                            hld.setStatus(Holding.Status.RESERVED);
                        }
                    }

                    return "reservation_success";
                }
            } else {
                reservations.validateHoldings(newRes, null);
            }
        }
        catch (NoHoldingsException e) {
            model.addAttribute("error", "nothingAvailable");
            return "reservation_error";
        }
        catch (ClosedException e) {
            model.addAttribute("error", "restricted");
            return "reservation_error";
        }
        model.addAttribute("reservation", newRes);

        return "reservation_create";
    }

    private ArrayList<String> getAllAvailableHoldingsInReservation(Reservation newRes) {
        List<HoldingReservation> holdingReservations = newRes.getHoldingReservations();
        ArrayList<String> listOfPids = new ArrayList<>();
        for (HoldingReservation hr : holdingReservations) {
            try {
                ExternalRecordInfo eri = lookup.getRecordMetaDataByPid(hr.getHolding().getRecord().getPid());
                String unitIdsFromContainer = lookup.getUnitIdsFromContainer(hr.getHolding().getRecord().getPid(), eri.getContainer(), false);
                String[] values = unitIdsFromContainer.split(",");
                for (String pid : values) {
                    if (!listOfPids.contains(pid)) {
                        listOfPids.add(pid);
                    }
                }
            }
            catch (NoSuchPidException e) {
                // ignore no pid error
            }
        }

        return listOfPids;
    }

    /***
     * Based on the codes given, returns the holdings of the given reservation that are either open or reserved.
     * @param reservation The reservation with holdings to check.
     * @param codes The codes giving access to certain restricted records.
     * @param returnRestricted Whether to return a list of restricted records, or open records.
     * @return The list with records matched.
     */
    private List<HoldingReservation> findHoldingsOnRestriction(Reservation reservation, String[] codes,
                                                               boolean returnRestricted) {
        List<HoldingReservation> foundHr = new ArrayList<>();
        for (HoldingReservation hr : reservation.getHoldingReservations()) {
            Record record = hr.getHolding().getRecord();
            ExternalRecordInfo.Restriction restriction = record.getRestriction();

            if (restriction == ExternalRecordInfo.Restriction.RESTRICTED) {
                boolean hasPermission = false;
                for (Permission permission : reservation.getPermissions()) {
                    if (permission.hasGranted(record)) {
                        hasPermission = true;

                        if (!returnRestricted) {
                            foundHr.add(hr);
                        }
                    }
                }

                if (returnRestricted && !hasPermission) {
                    foundHr.add(hr);
                }
            } else if (!returnRestricted && (restriction == ExternalRecordInfo.Restriction.OPEN)) {
                foundHr.add(hr);
            }
        }
        return foundHr;
    }

    /**
     * Checks the permission if applicable (i.e. holdings selected are tied to one or more restricted records).
     *
     * @param model       The model to add errors to.
     * @param perm        The permission, can be null if not applicable.
     * @param reservation The Reservation with holdings to check.
     * @return True iff the given permission (can be null) is allowed to reserve the provided holdings.
     */
    private boolean checkPermissions(Model model, Permission perm, Reservation reservation) {
        if (perm == null) {
            model.addAttribute("error", "invalidCode");
            return false;
        }

        for (HoldingReservation hr : reservation.getHoldingReservations()) {
            Record permRecord = hr.getHolding().getRecord();
            if (permRecord.getRestriction() == ExternalRecordInfo.Restriction.RESTRICTED) {
                if (perm.hasGranted(permRecord)) {
                    return true;
                }
            }
        }

        model.addAttribute("error", "invalidCode");
        return false;
    }

    /**
     * Checks the holdings of a request.
     *
     * @param model   The model to add errors to.
     * @param request The Request with holdings to check.
     * @return Whether no errors were found.
     */
    @Override
    protected boolean checkHoldings(Model model, Request request) {
        if (!super.checkHoldings(model, request)) {
            return false;
        }

        int maxItems = deliveryProperties.getReservationMaxItems();
        int maxChildren = deliveryProperties.getReservationMaxChildren();

        Map<String, Integer> noOfRequests = new HashMap<>();
        for (HoldingRequest hr : request.getHoldingRequests()) {
            Record record = hr.getHolding().getRecord();
            if (record.getParent() != null) {
                record = record.getParent();
            }

            if (noOfRequests.containsKey(record.getPid())) {
                noOfRequests.put(record.getPid(), noOfRequests.get(record.getPid()) + 1);
            } else {
                noOfRequests.put(record.getPid(), 1);
            }
        }

        if (noOfRequests.size() > maxItems) {
            model.addAttribute("error", "limitItems");
            return false;
        }

        for (int count : noOfRequests.values()) {
            if (count > maxChildren) {
                model.addAttribute("error", "limitChildren");
                return false;
            }
        }

        return true;
    }

    private List<HoldingReservation> uriPathToHoldingReservations(String path) {
        List<Holding> holdings = uriPathToHoldings(path, true);
        if (holdings == null) {
            return null;
        }

        List<HoldingReservation> hrs = new ArrayList<>();
        for (Holding holding : holdings) {
            HoldingReservation hr = new HoldingReservation();
            hr.setHolding(holding);
            hrs.add(hr);
        }
        return hrs;
    }

    /**
     * Print a reservation if it has been reserved between the opening and
     * closing times of the readingroom.
     * <p>
     * Run this in a separate thread, we do nothing on failure so in this case this is perfectly possible.
     * This speeds up the processing of the page for the end-user.
     *
     * @param res The reservation to print.
     */
    @Async
    protected void autoPrint(final Reservation res) {
        try {
            Date create = res.getCreationDate();
            Date access = res.getDate();

            // Do not print when not reserved on same day as access.
            if (access.after(create)) {
                return;
            }

            if (DateUtils.isBetweenOpeningAndClosingTime(deliveryProperties, create)) {
                reservations.printReservation(res);
            }
        }
        catch (PrinterException e) {
            log.warn("Printing reservation failed", e);
            // Do nothing, let an employee print it later on.
        }
    }
    // }}}

    // {{{ Batch process reservations

    /**
     * Mass delete reservations.
     *
     * @param req     The HTTP request object.
     * @param checked The reservations marked for deletion.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess",
            method = RequestMethod.POST,
            params = "delete")
    @PreAuthorize("hasRole('ROLE_RESERVATION_DELETE')")
    public String batchProcessDelete(HttpServletRequest req,
                                     @RequestParam(required = false) List<String>
                                             checked) {
        // Delete all the provided reservations
        if (checked != null) {
            ArrayList<Integer> requestIds = new ArrayList<>();

            for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
                if (!requestIds.contains(bulkActionIds.getRequestId())) {
                    requestIds.add(bulkActionIds.getRequestId());
                }
            }

            // loop each reservation
            for (Integer resId : requestIds) {
                changeStatusOfHoldingsInReservation(resId, Holding.Status.RESERVED, Holding.Status.AVAILABLE);
            }

            for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
                delete(bulkActionIds.getRequestId());
            }
        }

        String qs = req.getQueryString() != null ?
                "?" + req.getQueryString() : "";
        return "redirect:/reservation/" + qs;
    }

    private void changeStatusOfHoldingsInReservation(Integer reservationId, Holding.Status StatusFrom, Holding.Status StatusTo) {
        ArrayList<Integer> holdingIds = new ArrayList<>();

        Reservation r = reservations.getReservationById(reservationId);
        if (r == null) {
            return;
        }

        List<Holding> hlds = r.getHoldings();

        // get all holding id's in reservation
        for (Holding hld : hlds) {
            if (!holdingIds.contains(hld.getId())) {
                holdingIds.add(hld.getId());
            }
        }

        // change status for object/holdings in same doos/box
        ArrayList<String> listOfPids2 = getListOfHoldingsInSameDoos(reservationId, holdingIds);
        List<Holding> holdingList = getHoldingsWithSpecificStatus(listOfPids2, StatusFrom);
        if (holdingList != null) {
            for (Holding hld : holdingList) {
                hld.setStatus(StatusTo);
            }
        }
    }

    private ArrayList<String> getListOfHoldingsInSameDoos(Integer reservationId, ArrayList<Integer> holdingIds) {
        ArrayList<String> listOfPids = new ArrayList<>();

        Reservation r = reservations.getReservationById(reservationId);
        if (r == null) {
            return null;
        }

        for (Holding h : r.getHoldings()) {
            if (holdingIds.contains(h.getId())) {
                ExternalRecordInfo eri = null;
                String unitIdsFromContainer = null;
                try {
                    eri = lookup.getRecordMetaDataByPid(h.getRecord().getPid());
                    unitIdsFromContainer = lookup.getUnitIdsFromContainer(h.getRecord().getPid(), eri.getContainer(), true);

                    String[] values = unitIdsFromContainer.split(",");
                    for (String value : values) {
                        if (!listOfPids.contains(value)) {
                            listOfPids.add(value);
                        }
                    }


                }
                catch (NoSuchPidException e) {
                    e.printStackTrace();
                }
            }
        }

        return listOfPids;
    }

    /**
     * Show print marked holdings (except already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reservations.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess",
            method = RequestMethod.POST,
            params = "print")
    public String batchProcessPrint(HttpServletRequest req,
                                    @RequestParam(required = false) List<String>
                                            checked) {
        String qs = req.getQueryString() != null ?
                "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reservation/" + qs;
        }

        List<HoldingReservation> hrs = new ArrayList<>();
        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Reservation r = reservations.getReservationById(bulkActionIds.getRequestId());
            for (HoldingReservation hr : r.getHoldingReservations()) {
                if (hr.getHolding().getId() == bulkActionIds.getHoldingId()) {
                    hrs.add(hr);
                }
            }

            if (!hrs.isEmpty()) {
                try {
                    reservations.printItems(hrs, false);
                }
                catch (PrinterException e) {
                    return "reservation_print_failure";
                }
            }
        }

        return "redirect:/reservation/" + qs;
    }

    /**
     * Show print marked holdings (including already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reservations.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess",
            method = RequestMethod.POST,
            params = "printForce")
    public String batchProcessPrintForce(HttpServletRequest req,
                                         @RequestParam(required = false) List<String>
                                                 checked) {
        String qs = req.getQueryString() != null ?
                "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reservation/" + qs;
        }

        List<HoldingReservation> hrs = new ArrayList<>();
        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Reservation r = reservations.getReservationById(bulkActionIds.getRequestId());
            for (HoldingReservation hr : r.getHoldingReservations()) {
                if (hr.getHolding().getId() == bulkActionIds.getHoldingId()) {
                    hrs.add(hr);
                }
            }
        }

        if (!hrs.isEmpty()) {
            try {
                reservations.printItems(hrs, true);
            }
            catch (PrinterException e) {
                return "reservation_print_failure";
            }
        }

        return "redirect:/reservation/" + qs;
    }

    /**
     * Change status of marked reservations
     *
     * @param req       The HTTP request object.
     * @param checked   The reservations marked.
     * @param newStatus The status the selected reservations should be set to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess",
            method = RequestMethod.POST,
            params = "changeStatus")
    @PreAuthorize("hasRole('ROLE_RESERVATION_MODIFY')")
    public String batchProcessChangeStatus(HttpServletRequest req,
                                           @RequestParam(required = false) List<String>
                                                   checked,
                                           @RequestParam Reservation.Status
                                                   newStatus) {
        String qs = req.getQueryString() != null ?
                "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reservation/" + qs;
        }

        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Reservation r = reservations.getReservationById(bulkActionIds.getRequestId());

            // Only change reservations which exist.
            if (r == null) {
                continue;
            }

            // Set the new status and holding statuses.
            reservations.updateStatusAndAssociatedHoldingStatus(r, newStatus);
            reservations.saveReservation(r);
        }

        return "redirect:/reservation/" + qs;
    }

    /**
     * Change status of marked holdings.
     *
     * @param req              The HTTP request object.
     * @param checked          The holdings marked.
     * @param newHoldingStatus The status the selected holdings should be set to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "changeHoldingStatus")
    @PreAuthorize("hasRole('ROLE_RESERVATION_MODIFY')")
    public String batchProcessChangeHoldingStatus(HttpServletRequest req,
                                                  @RequestParam(required = false) List<String> checked,
                                                  @RequestParam Holding.Status newHoldingStatus) {
        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no holdings were selected
        if (checked == null) {
            return "redirect:/reservation/" + qs;
        }

        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Holding h = records.getHoldingById(bulkActionIds.getHoldingId());
            if (h == null) {
                continue;
            }

            // Only update the status if the holding is active for the same reservation
            Request request = requests.getActiveFor(h);
            if ((request instanceof Reservation) &&
                    (((Reservation) request).getId() == bulkActionIds.getRequestId())) {
                // Set the new status
                requests.updateHoldingStatus(h, newHoldingStatus);
                records.saveHolding(h);
            }
        }

        return "redirect:/reservation/" + qs;
    }

    // }}}
    // {{{ Delete API

    /**
     * Delete reservations.
     *
     * @param id The identifier of the reservation to delete
     */
    private void delete(int id) {
        Reservation rs = reservations.getReservationById(id);
        if (rs != null) {
            reservations.removeReservation(rs);
        }
    }

    /**
     * Delete a reservation (Method DELETE).
     *
     * @param id The id of the reservation to delete.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}",
            method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_RESERVATION_DELETE')")
    public String apiDelete(@PathVariable int id) {
        delete(id);
        return "";
    }

    /**
     * Delete a reservation (Method POST, !DELETE in path).
     *
     * @param id The id of the reservation to delete.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id}!DELETE",
            method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_RESERVATION_DELETE')")
    public String apiFakeDelete(@PathVariable int id) {
        delete(id);
        return "";
    }
    // }}}
    // {{{ Model data

    /**
     * Map representation of status types of reservations for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("status_types")
    public Map<String, Reservation.Status> statusTypes() {
        Map<String, Reservation.Status> data = new LinkedHashMap<>();
        data.put("PENDING", Reservation.Status.PENDING);
        data.put("ACTIVE", Reservation.Status.ACTIVE);
        data.put("COMPLETED", Reservation.Status.COMPLETED);
        return data;
    }
    // }}}

    // {{{ Mass Create

    /**
     * Create a reservation without restrictions of size or usage.
     *
     * @param fromReservationId The id of a reservation to use as a base of
     *                          this new reservation, if applicable (not required).
     * @param model             The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform",
            method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RESERVATION_CREATE')")
    public String showMassCreateForm(@RequestParam(required = false)
                                             Integer fromReservationId, Model model) {
        Reservation newRes = new Reservation();
        if (fromReservationId != null) {
            Reservation fromReservation = reservations.getReservationById(fromReservationId);
            if (fromReservation != null) {
                newRes.setVisitorEmail(fromReservation.getVisitorEmail());
                newRes.setVisitorName(fromReservation.getVisitorName());
            }
        }
        model.addAttribute("reservation", newRes);
        return "reservation_mass_create";
    }

    /**
     * Process the search for new holdings to add to the mass reservation.
     *
     * @param newRes          The already semi-built reservation.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform",
            method = RequestMethod.POST,
            params = "searchSubmit")
    @PreAuthorize("hasRole('ROLE_RESERVATION_CREATE')")
    public String processSearchMassCreateForm(@ModelAttribute("reservation")
                                                      Reservation newRes,
                                              @RequestParam String searchTitle,
                                              @RequestParam String searchSignature,
                                              Model model) {
        List<Holding> holdingList = searchMassCreate(newRes, searchTitle, searchSignature);

        model.addAttribute("reservation", newRes);
        model.addAttribute("holdingList", holdingList);
        return "reservation_mass_create";
    }


    /**
     * Process the search for new holdings to add to the mass reservation.
     *
     * @param newRes          The already semi-built reservation.
     * @param result          The object to save the validation errors.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param print           Whether or not to print this reservation.
     * @param mail            Whether or not to mail a reservation confirmation.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform",
            method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_RESERVATION_CREATE')")
    public String processMassCreateForm(@ModelAttribute("reservation")
                                                Reservation newRes,
                                        BindingResult result,
                                        @RequestParam String searchTitle,
                                        @RequestParam(required = false) String searchSignature,
                                        @RequestParam(required = false)
                                                Boolean print,
                                        Boolean mail,
                                        Model model) {
        List<Holding> holdingList = searchMassCreate(newRes, searchTitle, searchSignature);

        try {

            reservations.createOrEdit(newRes, null, result);
            if (!result.hasErrors()) {
                if (print != null) {
                    reservations.printReservation(newRes);
                }
                if (mail != null) {
                    resMailer.mailConfirmation(newRes);
                }
                return "redirect:/reservation/" + newRes.getId();
            }
        }
        catch (ClosedException e) {
//            String msg =  msgSource.getMessage("reservation.error" +
            String msg = messageSource.getMessage("reservation.error" +
                            ".restricted", null,
                    "", LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result
                    .getObjectName(), null, null, msg));
        }
        catch (NoHoldingsException e) {
//            String msg =  msgSource.getMessage("reservation.error" +
            String msg = messageSource.getMessage("reservation.error" +
                            ".noHoldings", null,
                    "", LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result
                    .getObjectName(), null, null, msg));
        }
        catch (PrinterException e) {
            // Do nothing if printing fails.
            // You will see the printed flag being false in the overview.
            log.warn("Printing reservation failed", e);
        }


        model.addAttribute("reservation", newRes);
        model.addAttribute("holdingList", holdingList);
        return "reservation_mass_create";
    }
    // }}}

    /**
     * Get a list with the number of materials reserved per day.
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/materials", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_RESERVATION_VIEW')")
    public String reservationMaterials(HttpServletRequest req, Model model) {
        Map<String, String[]> p = req.getParameterMap();

        CriteriaBuilder cbMaterials = reservations.getHoldingReservationCriteriaBuilder();
        ReservationMaterialStatistics materialStatistics = new ReservationMaterialStatistics(cbMaterials, p);
        CriteriaQuery<Tuple> materialsCq = materialStatistics.tuple();

        CriteriaBuilder cbSignature = reservations.getHoldingReservationCriteriaBuilder();
        ReservationSignatureStatistics signatureStatistics = new ReservationSignatureStatistics(cbSignature, p);
        CriteriaQuery<Tuple> signatuesCq = signatureStatistics.tuple();
        List<Tuple> signatureTuples = reservations.listTuples(signatuesCq);

        Map<String, RecordCount> parentSignaturesMap = signatureTuples.stream().collect(Collectors.toMap(
                t -> (t.get("parentSignature") != null) ? t.get("parentSignature").toString() : t.get("signature").toString(),
                t -> new RecordCount(
                        (t.get("parentTitle") != null) ? t.get("parentTitle").toString() : t.get("title").toString(),
                        new Long(t.get("numberOfRequests").toString())
                ),
                (num1, num2) -> new RecordCount(num1.title, num1.count + num2.count)
        )).entrySet().stream().sorted((c1, c2) -> {
            int compared = c1.getValue().count.compareTo(c2.getValue().count) * -1;
            return (compared == 0) ? c1.getKey().compareToIgnoreCase(c2.getKey()) : compared;
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

        model.addAttribute("materialTuples", reservations.listTuples(materialsCq));
        model.addAttribute("parentSignaturesMap", parentSignaturesMap);
        model.addAttribute("signatureTuples", signatureTuples);

        return "reservation_materials";
    }

    public final class RecordCount {
        public String title;
        public Long count;

        RecordCount(String title, Long count) {
            this.title = title;
            this.count = count;
        }

        public String getTitle() {
            return title;
        }

        public Long getCount() {
            return count;
        }
    }
}
