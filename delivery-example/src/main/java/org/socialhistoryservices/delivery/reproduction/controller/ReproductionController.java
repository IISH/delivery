package org.socialhistoryservices.delivery.reproduction.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.socialhistoryservices.delivery.InvalidRequestException;
import org.socialhistoryservices.delivery.ResourceNotFoundException;
import org.socialhistoryservices.delivery.api.PayWayMessage;
import org.socialhistoryservices.delivery.api.PayWayService;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.service.PermissionService;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.reproduction.entity.*;
import org.socialhistoryservices.delivery.reproduction.entity.Order;
import org.socialhistoryservices.delivery.reproduction.service.IncompleteOrderDetailsException;
import org.socialhistoryservices.delivery.reproduction.service.OrderRegistrationFailureException;
import org.socialhistoryservices.delivery.reproduction.service.ReproductionMailer;
import org.socialhistoryservices.delivery.reproduction.service.ReproductionService;
import org.socialhistoryservices.delivery.reproduction.util.ReproductionStandardOptions;
import org.socialhistoryservices.delivery.request.controller.AbstractRequestController;
import org.socialhistoryservices.delivery.request.service.ClosedException;
import org.socialhistoryservices.delivery.request.service.InUseException;
import org.socialhistoryservices.delivery.request.service.NoHoldingsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.awt.print.PrinterException;
import java.beans.PropertyEditorSupport;
import java.util.*;

/**
 * Controller of the Reproducion package, handles all /reproduction/* requests.
 */
@Controller
@Transactional
@RequestMapping(value = "/reproduction")
public class ReproductionController extends AbstractRequestController {
    private static final Log LOGGER = LogFactory.getLog(ReproductionController.class);

    @Autowired
    private ReproductionService reproductions;

    @Autowired
    private PermissionService permissions;

    @Autowired
    private ReproductionMailer reproductionMailer;

    @Autowired
    private PayWayService payWayService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);

        // This is needed for passing a reproduction standard option ID
        binder.registerCustomEditor(ReproductionStandardOption.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                int id = Integer.parseInt(text);
                ReproductionStandardOption reproductionStandardOption =
                        reproductions.getReproductionStandardOptionById(id);
                setValue(reproductionStandardOption);
            }
        });
    }

    /**
     * Map representation of status types of reproductions for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("status_types")
    public Map<String, Reproduction.Status> statusTypes() {
        Map<String, Reproduction.Status> data = new LinkedHashMap<String, Reproduction.Status>();
        data.put("WAITING_FOR_ORDER_DETAILS", Reproduction.Status.WAITING_FOR_ORDER_DETAILS);
        data.put("HAS_ORDER_DETAILS", Reproduction.Status.HAS_ORDER_DETAILS);
        data.put("ORDER_CREATED", Reproduction.Status.ORDER_CREATED);
        data.put("PAYED", Reproduction.Status.PAYED);
        data.put("PENDING", Reproduction.Status.PENDING);
        data.put("ACTIVE", Reproduction.Status.ACTIVE);
        data.put("COMPLETED", Reproduction.Status.COMPLETED);
        data.put("DELIVERED", Reproduction.Status.DELIVERED);
        return data;
    }

    /**
     * Map representation of SOR level for use in views.
     *
     * @return The map.
     */
    @ModelAttribute("levels")
    public Map<String, ReproductionStandardOption.Level> levels() {
        Map<String, ReproductionStandardOption.Level> data = new LinkedHashMap<String, ReproductionStandardOption.Level>();
        data.put("MASTER", ReproductionStandardOption.Level.MASTER);
        data.put("LEVEL1", ReproductionStandardOption.Level.LEVEL1);
        data.put("LEVEL2", ReproductionStandardOption.Level.LEVEL2);
        data.put("LEVEL3", ReproductionStandardOption.Level.LEVEL3);
        return data;
    }

    /**
     * Map representation of SOR level for use in views.
     *
     * @return The map.
     */
    @ModelAttribute("materialTypes")
    public Map<String, ExternalRecordInfo.MaterialType> materialTypes() {
        Map<String, ExternalRecordInfo.MaterialType> data = new LinkedHashMap<String, ExternalRecordInfo.MaterialType>();
        data.put("ARTICLE", ExternalRecordInfo.MaterialType.ARTICLE);
        data.put("SERIAL", ExternalRecordInfo.MaterialType.SERIAL);
        data.put("BOOK", ExternalRecordInfo.MaterialType.BOOK);
        data.put("SOUND", ExternalRecordInfo.MaterialType.SOUND);
        data.put("DOCUMENTATION", ExternalRecordInfo.MaterialType.DOCUMENTATION);
        data.put("ARCHIVE", ExternalRecordInfo.MaterialType.ARCHIVE);
        data.put("VISUAL", ExternalRecordInfo.MaterialType.VISUAL);
        data.put("MOVING_VISUAL", ExternalRecordInfo.MaterialType.MOVING_VISUAL);
        data.put("OTHER", ExternalRecordInfo.MaterialType.OTHER);
        return data;
    }

    /**
     * Fetches one specific reproduction.
     *
     * @param id    ID of the reproduction to fetch.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id:[\\d]+}", method = RequestMethod.GET)
    @Secured("ROLE_REPRODUCTION_VIEW")
    public String getSingle(@PathVariable int id, Model model) {
        Reproduction r = reproductions.getReproductionById(id);
        if (r == null) {
            throw new ResourceNotFoundException();
        }
        model.addAttribute("reproduction", r);
        return "reproduction_get";
    }

    /**
     * Get a list of reproductions.
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @Secured("ROLE_REPRODUCTION_VIEW")
    public String get(HttpServletRequest req, Model model) {
        Map<String, String[]> p = req.getParameterMap();

        CriteriaBuilder cb = reproductions.getHoldingReproductionCriteriaBuilder();
        CriteriaQuery<HoldingReproduction> cq = cb.createQuery(HoldingReproduction.class);
        Root<HoldingReproduction> hrRoot = cq.from(HoldingReproduction.class);
        cq.select(hrRoot);

        Join<HoldingReproduction, Reproduction> rRoot = hrRoot.join(HoldingReproduction_.reproduction);

        // Expression to be the where clause of the query
        Expression<Boolean> where = null;

        // Filters
        where = addDateFilter(p, cb, rRoot, where);
        where = addNameFilter(p, cb, rRoot, where);
        where = addEmailFilter(p, cb, rRoot, where);
        where = addStatusFilter(p, cb, rRoot, where);
        where = addPrintedFilter(p, cb, rRoot, where);
        where = addSearchFilter(p, cb, hrRoot, rRoot, where);

        // Set the where clause
        if (where != null) {
            cq.where(where);
        }

        Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);

        cq.orderBy(parseSortFilter(p, cb, rRoot, hRoot));

        // Fetch result set
        List<HoldingReproduction> hList = reproductions.listHoldingReproductions(cq);
        PagedListHolder<HoldingReproduction> pagedListHolder = new PagedListHolder<HoldingReproduction>(hList);
        initOverviewModel(p, model, pagedListHolder);

        return "reproduction_get_list";
    }

    /**
     * Add the date/from_date/to_date filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addDateFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                              Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        Date date = getDateFilter(p);
        if (date != null) {
            Expression<Boolean> exDate = cb.between(rRoot.<Date>get(Reproduction_.date), date, date);
            where = (where != null) ? cb.and(where, exDate) : exDate;
        } else {
            Date fromDate = getFromDateFilter(p);
            Date toDate = getToDateFilter(p);
            if (fromDate != null) {
                Expression<Boolean> exDate = cb.greaterThanOrEqualTo(rRoot.<Date>get(Reproduction_.date), fromDate);
                where = (where != null) ? cb.and(where, exDate) : exDate;
            }
            if (toDate != null) {
                Expression<Boolean> exDate = cb.lessThanOrEqualTo(rRoot.<Date>get(Reproduction_.date), toDate);
                where = (where != null) ? cb.and(where, exDate) : exDate;
            }
        }
        return where;
    }

    /**
     * Add the name filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addNameFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                              Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        if (p.containsKey("customerName")) {
            Expression<Boolean> exName = cb.like(rRoot.<String>get(Reproduction_.customerName),
                    "%" + p.get("customerName")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exName) : exName;
        }
        return where;
    }

    /**
     * Add the email filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addEmailFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                               Join<HoldingReproduction, Reproduction> rRoot,
                                               Expression<Boolean> where) {
        if (p.containsKey("customerEmail")) {
            Expression<Boolean> exEmail = cb.like(rRoot.get(Reproduction_.customerEmail),
                    "%" + p.get("customerEmail")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exEmail) : exEmail;
        }
        return where;
    }

    /**
     * Add the status filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addStatusFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("status")) {
            String status = p.get("status")[0].trim().toUpperCase();
            if (!status.equals("")) { // Tolerant to empty status
                try {
                    Expression<Boolean> exStatus = cb.equal(rRoot.<Reproduction.Status>get(Reproduction_.status),
                            Reproduction.Status.valueOf(status));
                    where = (where != null) ? cb.and(where, exStatus) : exStatus;
                } catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such status: " + status);
                }
            }
        }
        return where;
    }

    /**
     * Add the printed filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addPrintedFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                 Join<HoldingReproduction, Reproduction> rRoot,
                                                 Expression<Boolean> where) {
        if (p.containsKey("printed")) {
            String printed = p.get("printed")[0].trim().toLowerCase();
            if (printed.isEmpty()) {
                return where;
            }

            Expression<Boolean> exPrinted = cb.equal(rRoot.<Boolean>get(Reproduction_.printed),
                    Boolean.parseBoolean(p.get("printed")[0]));
            where = (where != null) ? cb.and(where, exPrinted) : exPrinted;
        }
        return where;
    }

    /**
     * Add the search filter to the where clause, if present.
     *
     * @param p      The parameter list to search the given filter value in.
     * @param cb     The criteria builder.
     * @param hrRoot The holding reproduction root.
     * @param rRoot  The reproduction root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addSearchFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                Root<HoldingReproduction> hrRoot,
                                                Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("search") && !p.get("search")[0].trim().equals("")) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);
            Join<Holding, Record> recRoot = hRoot.join(Holding_.record);
            Join<Record, ExternalRecordInfo> eRoot = recRoot.join(Record_.externalInfo);

            Expression<Boolean> exSearch = cb.or(
                    cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)), "%" + search + "%"),
                    cb.like(cb.lower(rRoot.<String>get(Reproduction_.customerName)), "%" + search + "%"),
                    cb.like(cb.lower(rRoot.<String>get(Reproduction_.customerEmail)), "%" + search + "%"),
                    cb.like(cb.lower(hRoot.<String>get(Holding_.signature)), "%" + search + "%")
            );

            where = (where != null) ? cb.and(where, exSearch) : exSearch;
        }
        return where;
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query.
     *
     * @param p     The parameter list to search the filter values in.
     * @param cb    The criteria builder used to construct the Order.
     * @param rRoot The root of the reservation used to construct the Order.
     * @param hRoot The root of the holding used to construct the Order.
     * @return The order the query should be in (asc/desc) sorted on provided column. Defaults to asc on the PK column.
     */
    private javax.persistence.criteria.Order parseSortFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                             Join<HoldingReproduction, Reproduction> rRoot,
                                                             Join<HoldingReproduction, Holding> hRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression e = rRoot.get(Reproduction_.creationDate);

        if (containsSort) {
            String sort = p.get("sort")[0];
            if (sort.equals("customerName")) {
                e = rRoot.get(Reproduction_.customerName);
            } else if (sort.equals("customerEmail")) {
                e = rRoot.get(Reproduction_.customerEmail);
            } else if (sort.equals("status")) {
                e = rRoot.get(Reproduction_.status);
            } else if (sort.equals("printed")) {
                e = rRoot.get(Reproduction_.printed);
            } else if (sort.equals("signature")) {
                e = hRoot.get(Holding_.signature);
            } else if (sort.equals("holdingStatus")) {
                e = hRoot.get(Holding_.status);
            }
        }

        if (containsSortDir && p.get("sort_dir")[0].toLowerCase().equals("asc")) {
            return cb.asc(e);
        }
        return cb.desc(e);
    }

    /**
     * Mass delete reproductions.
     *
     * @param req     The HTTP request object.
     * @param checked The reproductions marked for deletion.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "delete")
    @Secured("ROLE_REPRODUCTION_DELETE")
    public String batchProcessDelete(HttpServletRequest req, @RequestParam(required = false) Set<Integer> checked) {
        // Delete all the provided reproductions
        if (checked != null) {
            for (int id : checked) {
                Reproduction r = reproductions.getReproductionById(id);
                if (r != null) {
                    reproductions.removeReproduction(r);
                }
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
    }

    /**
     * Show print marked reproductions (except already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reproductions.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "print")
    public String batchProcessPrint(HttpServletRequest req, @RequestParam(required = false) Set<Integer> checked) {
        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reproduction/" + qs;
        }

        for (int id : checked) {
            Reproduction r = reproductions.getReproductionById(id);
            if (r != null) {
                try {
                    reproductions.printReproduction(r);
                } catch (PrinterException e) {
                    return "reproduction_print_failure";
                }
            }
        }

        return "redirect:/reproduction/" + qs;
    }

    /**
     * Show print marked reproductions (including already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reproductions.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "printForce")
    public String batchProcessPrintForce(HttpServletRequest req, @RequestParam(required = false) Set<Integer> checked) {
        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reproduction/" + qs;
        }

        for (int id : checked) {
            Reproduction r = reproductions.getReproductionById(id);
            if (r != null) {
                try {
                    reproductions.printReproduction(r, true);
                } catch (PrinterException e) {
                    return "reproduction_print_failure";
                }
            }
        }

        return "redirect:/reproduction/" + qs;
    }

    /**
     * Change status of marked reproductions
     *
     * @param req       The HTTP request object.
     * @param checked   The reproductions marked.
     * @param newStatus The status the selected reproductions should be set to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "changeStatus")
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String batchProcessChangeStatus(HttpServletRequest req, @RequestParam(required = false) Set<Integer> checked,
                                           @RequestParam Reproduction.Status newStatus) {
        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";

        // Simply redirect to previous page if no reservations were selected
        if (checked == null) {
            return "redirect:/reproduction/" + qs;
        }

        for (int id : checked) {
            Reproduction r = reproductions.getReproductionById(id);

            // Only change reproductions which exist.
            if (r != null) {
                continue;
            }

            // Set the new status and holding statuses.
            r.updateStatusAndAssociatedHoldingStatus(newStatus);
            reproductions.saveReproduction(r);
        }

        return "redirect:/reproduction/" + qs;
    }

    /**
     * Show the create form of a reproduction.
     *
     * @param req   The HTTP request.
     * @param path  The pid/signature string (URL encoded).
     * @param code  The permission code to use for restricted records. TODO: ?
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}", method = RequestMethod.GET)
    public String showCreateForm(HttpServletRequest req, @PathVariable String path,
                                 @RequestParam(required = false) String code, Model model) {
        Reproduction reproduction = new Reproduction();
        reproduction.setHoldingReproductions(uriPathToHoldingReproductions(path));
        return processReservationCreation(req, reproduction, null, code, model, false);
    }

    /**
     * Process the create form of a reproduction.
     *
     * @param req    The HTTP request.
     * @param newRep The submitted reproduction.
     * @param result The binding result to put errors in.
     * @param code   The permission code to use for restricted records.
     * @param model  The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}", method = RequestMethod.POST)
    public String processCreateForm(HttpServletRequest req, @ModelAttribute("reproduction") Reproduction newRep,
                                    BindingResult result, @RequestParam(required = false) String code, Model model) {
        return processReservationCreation(req, newRep, result, code, model, true);
    }

    private List<HoldingReproduction> uriPathToHoldingReproductions(String path) {
        List<Holding> holdings = uriPathToHoldings(path, false);
        if (holdings == null)
            return null;

        List<HoldingReproduction> hrs = new ArrayList<HoldingReproduction>();
        for (Holding holding : holdings) {
            HoldingReproduction hr = new HoldingReproduction();
            hr.setHolding(holding);
            hrs.add(hr);
        }
        return hrs;
    }

    private String processReservationCreation(HttpServletRequest req, Reproduction reproduction, BindingResult result,
                                              String permission, Model model, boolean commit) {
        if (!checkHoldings(model, reproduction))
            return "reproduction_error";

        Permission perm = permissions.getPermissionByCode(permission);
        if (!checkPermissions(model, perm, reproduction))
            return "reproduction_choice";

        if (perm != null) {
            if (!commit) {
                reproduction.setCustomerName(perm.getName());
                reproduction.setCustomerEmail(perm.getEmail());
            } else {
                // TODO: reproduction.setPermission(perm);
            }

            /*if (reproduction.getDate() == null || !perm.isValidOn(newRes.getDate())) {
                    model.addAttribute("error", "notValidOnDate");
                    return "reservation_error";
            }*/
        }

        // Obtain all the standard reproduction options
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        try {
            if (commit) {
                // Make sure a Captcha was entered correctly.
                checkCaptcha(req, result, model);
                reproductions.createOrEdit(reproduction, null, result, true);
                if (!result.hasErrors()) {
                    return determineNextStep(reproduction, model);
                }
            } else {
                reproductions.validateHoldings(reproduction, null, false);
            }
        } catch (NoHoldingsException e) {
            throw new ResourceNotFoundException();
        } catch (InUseException e) {
            // TODO: Should not be trown.
        } catch (ClosedException e) {
            model.addAttribute("error", "restricted");
            return "reproduction_error";
        }
        model.addAttribute("reproduction", reproduction);

        return "reproduction_create";
    }

    private Map<String, List<ReproductionStandardOption>> obtainStandardReproductionOptions() {
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                new HashMap<String, List<ReproductionStandardOption>>();
        for (ReproductionStandardOption standardOption : reproductions.getAllReproductionStandardOptions()) {
            String materialType = standardOption.getMaterialType().name();
            List<ReproductionStandardOption> options = new ArrayList<ReproductionStandardOption>();
            if (reproductionStandardOptions.containsKey(materialType)) {
                options = reproductionStandardOptions.get(materialType);
            }
            options.add(standardOption);
            reproductionStandardOptions.put(materialType, options);
        }
        return reproductionStandardOptions;
    }

    private String determineNextStep(Reproduction reproduction, Model model) {
        if (reproduction.hasOrderDetails()) {
            // Mail the confirmation (order is ready) to the customer.
            try {
                reproductionMailer.mailOrderReady(reproduction);
            } catch (MailException me) {
                model.addAttribute("error", "mail");
            }

            return "redirect:/reproduction/confirm/" + reproduction.getId() + "/" + reproduction.getToken();
        }

        // Mail the reproduction details to the customer.
        try {
            reproductionMailer.mailPending(reproduction);
        } catch (MailException me) {
            model.addAttribute("error", "mail");
        }

        model.addAttribute("reproduction", reproduction);
        return "reproduction_pending";
    }

    /**
     * Show the confirmation form of a reproduction.
     *
     * @param req            The HTTP request.
     * @param reproductionId The id of the reproduction.
     * @param token          A token to prevent unauthorized access to the reproduction.
     * @param model          The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/confirm/{reproductionId:[\\d]+}/{token}", method = RequestMethod.GET)
    public String showConfirm(@PathVariable int reproductionId, @PathVariable String token, Model model) {
        Reproduction reproduction = reproductions.getReproductionById(reproductionId);
        validateToken(reproduction, token);
        return processConfirmation(null, reproduction, model, false);
    }

    /**
     * Process the confirmation form of a reproduction.
     *
     * @param req            The HTTP request.
     * @param reproductionId The id of the reproduction.
     * @param token          A token to prevent unauthorized access to the reproduction.
     * @param model          The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/confirm/{reproductionId:[\\d]+}/{token}", method = RequestMethod.POST)
    public String processConfrim(HttpServletRequest req, @PathVariable int reproductionId,
                                 @PathVariable String token, Model model) {
        Reproduction reproduction = reproductions.getReproductionById(reproductionId);
        validateToken(reproduction, token);
        return processConfirmation(req, reproduction, model, true);
    }

    private String processConfirmation(HttpServletRequest req, Reproduction reproduction, Model model, boolean commit) {
        if (reproduction == null)
            throw new InvalidRequestException("No such reproduction.");

        if (reproduction.getStatus().compareTo(Reproduction.Status.HAS_ORDER_DETAILS) < 0)
            throw new InvalidRequestException("Reproduction does not have all of the order details yet.");

        if (reproduction.getStatus() == Reproduction.Status.ORDER_CREATED) {
            Order order = reproduction.getOrder();
            if (order != null)
                return "redirect:" + payWayService.getPaymentPageRedirectLink(order.getId());
        }

        if (reproduction.getStatus().compareTo(Reproduction.Status.ORDER_CREATED) >= 0)
            throw new InvalidRequestException("Reproduction has been confirmed already.");

        model.addAttribute("reproduction", reproduction);
        if (commit) {
            String accept = req.getParameter("accept_terms_conditions");
            if (!"accept".equals(accept)) {
                String msg = msgSource.getMessage("accept.error", null, LocaleContextHolder.getLocale());
                model.addAttribute("acceptError", msg);
                return "reproduction_confirm";
            }

            try {
                // Change status to 'order created'
                reproduction.updateStatusAndAssociatedHoldingStatus(Reproduction.Status.ORDER_CREATED);
                Order order = reproductions.createOrder(reproduction);

                // If the reproduction is for free, take care of delivey
                if (reproduction.isForFree()) {
                    // TODO: Delivery of reproduction, redirect?
                    return "";
                }

                // Otherwise redirect the user to the payment page
                return "redirect:" + payWayService.getPaymentPageRedirectLink(order.getId());
            } catch (IncompleteOrderDetailsException onre) {
                // We already checked for this one though
                throw new InvalidRequestException("Reproduction is not ready yet.");
            } catch (OrderRegistrationFailureException orfe) {
                String msg = msgSource.getMessage("payway.error", null, LocaleContextHolder.getLocale());
                model.addAttribute("paywayError", msg);
            }
        }

        return "reproduction_confirm";
    }

    /**
     * PayWay response, payment was accepted.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/accept", method = RequestMethod.GET)
    public String accept() {
        return "reproduction_order_accept";
    }

    /**
     * A one time PayWay response after the payment has been made, in our case, to send an email.
     */
    @RequestMapping(value = "/order/accept", method = RequestMethod.POST)
    public HttpStatus accept(@RequestBody PayWayMessage payWayMessage) {
        // Is the received PayWay message valid? Make sure the customer actually payed
        if (!payWayService.isValid(payWayMessage)) {
            LOGGER.debug(String.format(
                    "/reproduction/order/accept : Invalid message received: %s", payWayMessage));
            return HttpStatus.BAD_REQUEST;
        }

        // Check the reproduction ...
        Integer reproductionId = payWayMessage.getInteger("userid");
        Reproduction reproduction = reproductions.getReproductionById(reproductionId);
        if (reproduction == null) {
            LOGGER.debug(String.format(
                    "/reproduction/order/accept : Reproduction not found for message %s", payWayMessage));
            return HttpStatus.BAD_REQUEST;
        }

        // ... and the order
        Integer orderId = payWayMessage.getInteger("orderid");
        Order order = reproduction.getOrder();
        if (order.getId() != orderId) {
            LOGGER.debug(String.format(
                    "/reproduction/order/accept : Reproduction order id does not match order id in message %s",
                    payWayMessage));
            return HttpStatus.BAD_REQUEST;
        }

        // Everything is fine, change status and send email to customer
        reproductions.refreshOrder(order);
        reproduction.updateStatusAndAssociatedHoldingStatus(Reproduction.Status.PAYED);
        try {
            reproductionMailer.mailPayed(reproduction);
        } catch (MailException me) {
            LOGGER.debug("/reproduction/order/accept : Failed to send payment confirmation email to customer");
        }

        return HttpStatus.OK;
    }

    /**
     * PayWay response, payment was canceled.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/cancel", method = RequestMethod.GET)
    public String cancel() {
        return "reproduction_order_cancel";
    }

    /**
     * PayWay response, payment was declined.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/decline", method = RequestMethod.GET)
    public String decline() {
        return "reproduction_order_decline";
    }

    /**
     * PayWay response, exception occurred during payment.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/exception", method = RequestMethod.GET)
    public String exception() {
        return "reproduction_order_exception";
    }

    /**
     * Create a reproduction without restrictions of size or usage.
     *
     * @param fromReproductionId The id of a reproduction to use as a base of this new reproduction,
     *                           if applicable (not required).
     * @param model              The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform", method = RequestMethod.GET)
    @Secured("ROLE_REPRODUCTION_CREATE")
    public String showMassCreateForm(@RequestParam(required = false) Integer fromReproductionId, Model model) {
        Reproduction newReproduction = new Reproduction();
        if (fromReproductionId != null) {
            Reproduction fromReproduction = reproductions.getReproductionById(fromReproductionId);
            if (fromReproduction != null) {
                newReproduction.setCustomerEmail(fromReproduction.getCustomerEmail());
                newReproduction.setCustomerName(fromReproduction.getCustomerName());
            }
        }
        model.addAttribute("reproduction", newReproduction);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Update a reproduction.
     *
     * @param id    ID of the reproduction to fetch.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/edit/{id:[\\d]+}", method = RequestMethod.GET)
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String showEditForm(@PathVariable int id, Model model) {
        Reproduction r = reproductions.getReproductionById(id);
        if (r == null) {
            throw new ResourceNotFoundException();
        }
        model.addAttribute("original", r);
        model.addAttribute("reproduction", r);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Process the search for new holdings to add to the mass reproduction.
     *
     * @param newReproduction The already semi-built reproduction.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform", method = RequestMethod.POST, params = "searchSubmit")
    @Secured("ROLE_REPRODUCTION_CREATE")
    public String processSearchMassCreateForm(@ModelAttribute("reproduction") Reproduction newReproduction,
                                              @RequestParam String searchTitle, @RequestParam String searchSignature,
                                              Model model) {
        List<Holding> holdingList = searchMassCreate(newReproduction, searchTitle, searchSignature);

        model.addAttribute("reproduction", newReproduction);
        model.addAttribute("holdingList", holdingList);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Process the search for new holdings to add to the mass reproduction.
     *
     * @param id              ID of the reproduction to fetch.
     * @param reproduction    The reproduction.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/edit/{id:[\\d]+}", method = RequestMethod.POST, params = "searchSubmit")
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String processSearchEditForm(@PathVariable int id, @ModelAttribute("reproduction") Reproduction reproduction,
                                        @RequestParam String searchTitle, @RequestParam String searchSignature,
                                        Model model) {
        Reproduction originalReproduction = reproductions.getReproductionById(id);
        if (originalReproduction == null) {
            throw new ResourceNotFoundException();
        }

        List<Holding> holdingList = searchMassCreate(reproduction, searchTitle, searchSignature);

        model.addAttribute("original", originalReproduction);
        model.addAttribute("reproduction", reproduction);
        model.addAttribute("holdingList", holdingList);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Save the new mass reproduction.
     *
     * @param newReproduction The already semi-built reproduction.
     * @param result          The object to save the validation errors.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param print           Whether or not to print this reproduction.
     * @param mail            Whether or not to mail a reproduction confirmation.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform", method = RequestMethod.POST)
    @Secured("ROLE_REPRODUCTION_CREATE")
    public String processMassCreateForm(@ModelAttribute("reproduction") Reproduction newReproduction,
                                        BindingResult result, @RequestParam String searchTitle,
                                        @RequestParam(required = false) String searchSignature, Boolean mail,
                                        Model model) {
        List<Holding> holdingList = searchMassCreate(newReproduction, searchTitle, searchSignature);

        try {
            reproductions.createOrEdit(newReproduction, null, result, false);
            if (!result.hasErrors()) {
                /*if (mail != null) {
                    resMailer.mailConfirmation(newRes);
                }*/
                return "redirect:/reproduction/" + newReproduction.getId();
            }
        } catch (ClosedException e) {
            String msg = msgSource.getMessage("reproduction.error.restricted", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        } catch (NoHoldingsException e) {
            String msg = msgSource.getMessage("reproduction.error.noHoldings", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        }

        model.addAttribute("reproduction", newReproduction);
        model.addAttribute("holdingList", holdingList);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Save the reproduction.
     *
     * @param id              ID of the reproduction to fetch.
     * @param reproduction    The reproduction.
     * @param result          The object to save the validation errors.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param print           Whether or not to print this reproduction.
     * @param mail            Whether or not to mail a reproduction confirmation.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/edit/{id:[\\d]+}", method = RequestMethod.POST)
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String processEditForm(@PathVariable int id, @ModelAttribute("reproduction") Reproduction reproduction,
                                  BindingResult result, @RequestParam String searchTitle,
                                  @RequestParam(required = false) String searchSignature, Boolean mail, Model model) {
        Reproduction originalReproduction = reproductions.getReproductionById(id);
        if (originalReproduction == null) {
            throw new ResourceNotFoundException();
        }

        List<Holding> holdingList = searchMassCreate(reproduction, searchTitle, searchSignature);

        try {
            reproductions.createOrEdit(reproduction, originalReproduction, result, false);
            if (!result.hasErrors()) {
                /*if (mail != null) {
                    resMailer.mailConfirmation(newRes);
                }*/
                return "redirect:/reproduction/" + originalReproduction.getId();
            }
        } catch (ClosedException e) {
            String msg = msgSource.getMessage("reproduction.error.restricted", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        } catch (NoHoldingsException e) {
            String msg = msgSource.getMessage("reproduction.error.noHoldings", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        }

        model.addAttribute("original", originalReproduction);
        model.addAttribute("reproduction", reproduction);
        model.addAttribute("holdingList", holdingList);
        model.addAttribute("reproductionStandardOptions", obtainStandardReproductionOptions());

        return "reproduction_mass_create";
    }

    /**
     * Displays all standard reproduction options for editing.
     *
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/standardoptions", method = RequestMethod.GET)
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String showStandardOptions(Model model) {
        ReproductionStandardOptions standardOptions =
                new ReproductionStandardOptions(reproductions.getAllReproductionStandardOptions());
        model.addAttribute("standardOptions", standardOptions);
        return "reproduction_standard_options_edit";
    }

    /**
     * Updates all standard reproductions options.
     *
     * @param model           The model to add response attributes to.
     * @param result          he object to save the validation errors.
     * @param standardOptions The standard reproduction options.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/standardoptions", method = RequestMethod.POST)
    @Secured("ROLE_REPRODUCTION_MODIFY")
    public String editStandardOptions(@ModelAttribute("standardOptions") ReproductionStandardOptions standardOptions,
                                      BindingResult result, Model model) {
        reproductions.editStandardOptions(standardOptions, result);
        model.addAttribute("standardOptions", standardOptions);
        return "reproduction_standard_options_edit";
    }

    /**
     * Print a reproduction if it currently is between the opening and closing times of the readingroom.
     *
     * @param reproduction The reproduction to print.
     */
    private void autoPrint(final Reproduction reproduction) {
        if (isBetweenOpeningAndClosingTime(new Date())) {
            // Run this in a separate thread, we do nothing on failure so in this case this is perfectly possible.
            // This speeds up the processing of the page for the end-user.
            new Thread(new Runnable() {
                public void run() {
                    try {
                        reproductions.printReproduction(reproduction);
                    } catch (PrinterException e) {
                        // Do nothing, let an employee print it later on.
                    }
                }
            }).start();
        }
    }

    private void validateToken(Reproduction reproduction, String token) {
        if ((reproduction != null) && !reproduction.getToken().equalsIgnoreCase(token))
            throw new InvalidRequestException("Invalid token provided.");
    }
}