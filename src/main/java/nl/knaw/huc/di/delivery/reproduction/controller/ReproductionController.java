package nl.knaw.huc.di.delivery.reproduction.controller;

import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionStandardOption;
import nl.knaw.huc.di.delivery.reproduction.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import nl.knaw.huc.di.delivery.record.service.RecordService;
import nl.knaw.huc.di.delivery.request.service.GeneralRequestService;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import nl.knaw.huc.di.delivery.reservation.service.ReservationService;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.config.ResourceNotFoundException;
import nl.knaw.huc.di.delivery.config.TemplatePreparationException;
import nl.knaw.huc.di.delivery.reproduction.entity.Order;
import nl.knaw.huc.di.delivery.reproduction.util.ReproductionStandardOptions;
import nl.knaw.huc.di.delivery.request.controller.AbstractRequestController;
import nl.knaw.huc.di.delivery.request.entity.HoldingRequest;
import nl.knaw.huc.di.delivery.request.entity.Request;
import nl.knaw.huc.di.delivery.request.service.ClosedException;
import nl.knaw.huc.di.delivery.request.service.NoHoldingsException;
import nl.knaw.huc.di.delivery.request.util.BulkActionIds;
import nl.knaw.huc.di.delivery.services.DetermineHumanService;
import nl.knaw.huc.di.delivery.services.UrlDecoderService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.util.*;

/**
 * Controller of the Reproduction package, handles all /reproduction/* requests.
 */
@Controller
@RequestMapping(value = "/reproduction")
public class ReproductionController extends AbstractRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReproductionController.class);

    private final ReproductionService reproductionService;
    private final ReservationService reservationService;
    private final ReproductionMailer reproductionMailer;
    private final ReproductionPDF reproductionPDF;
    private final MessageSource messageSource;
    private final RecordService recordService;
    private final GeneralRequestService generalRequestService;
    private final DetermineHumanService determineHumanService;


    public ReproductionController(ReproductionService reproductionService, ReservationService reservationService, ReproductionMailer reproductionMailer, ReproductionPDF reproductionPDF, MessageSource messageSource, GeneralRequestService generalRequestService, RecordService recordService, DetermineHumanService determineHumanService, DeliveryProperties deliveryProperties, UrlDecoderService urlDecoderService) {
        super(deliveryProperties, messageSource, generalRequestService, recordService, urlDecoderService);
        this.reproductionService = reproductionService;
        this.reservationService = reservationService;
        this.reproductionMailer = reproductionMailer;
        this.reproductionPDF = reproductionPDF;
        this.messageSource = messageSource;
        this.recordService = recordService;
        this.generalRequestService = generalRequestService;
        this.determineHumanService = determineHumanService;
    }

    /**
     * Fetches one specific reproduction.
     *
     * @param id    ID of the reproduction to fetch.
     * @param model Passed view model.
     * @param error In case of an error.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id:[\\d]+}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_VIEW')")
    public String getSingle(@PathVariable int id, Model model, @RequestParam(required = false) String error) {
        Reproduction r = reproductionService.getReproductionById(id);
        if (r == null) {
            throw new ResourceNotFoundException();
        }

        model.addAttribute("reproduction", r);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(r.getHoldings()));

        // Was there an email error?
        if (error != null)
            model.addAttribute("error", error);

        return "reproduction_get";
    }

    /**
     * Generate the invoice of a reproduction.
     *
     * @param id ID of the reproduction to fetch.
     */
    @RequestMapping(value = "/{id:[\\d]+}/invoice", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_VIEW')")
    public ResponseEntity<byte[]> getInvoice(@PathVariable int id) {
        try {
            Reproduction reproduction = reproductionService.getReproductionById(id);
            if (reproduction == null)
                throw new ResourceNotFoundException();

            byte[] pdf = reproductionPDF.getInvoice(reproduction, reproduction.getRequestLocale());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
            headers.setContentLength(pdf.length);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (TemplatePreparationException tpe) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a list of reproductions.
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_VIEW')")
    public String get(HttpServletRequest req, Model model, Pageable pageable) {
        Map<String, String[]> p = req.getParameterMap();
        CriteriaBuilder cb = reproductionService.getHoldingReproductionCriteriaBuilder();

        ReproductionSearch search = new ReproductionSearch(cb, p);
        CriteriaQuery<HoldingReproduction> cq = search.list();

        // Fetch result set
        Page<HoldingReproduction> pagedItem = reproductionService.findAll(cq, pageable);
        model.addAttribute("pagedItem", pagedItem);

        // Fetch holding active request information
        Set<Holding> holdings = getHoldings(pagedItem.stream().toList());
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(holdings));

        return "reproduction_get_list";
    }

    /**
     * Mass delete reproductions.
     *
     * @param req     The HTTP request object.
     * @param checked The reproductions marked for deletion.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "delete")
    @PreAuthorize("hasRole('REPRODUCTION_DELETE')")
    public String batchProcessDelete(HttpServletRequest req, @RequestParam(required = false) List<String> checked) {
        // Delete all the provided reproductions
        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Reproduction r = reproductionService.getReproductionById(bulkActionIds.getRequestId());
            if (r != null) {
                reproductionService.removeReproduction(r);
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
    }

    /**
     * Merge holdings for reproductions.
     *
     * @param req     The HTTP request object.
     * @param checked The reproductions marked for the merge.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "merge")
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String batchProcessMerge(HttpServletRequest req, @RequestParam(required = false) List<String> checked) {
        final String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";

        final List<HoldingReproduction> hrs = getHoldingReproductionsForBulk(checked);
        if (hrs.size() >1) {
            final Set<Reproduction> _reproductions = new HashSet<>();
            for (HoldingReproduction hr : hrs) {
                final Reproduction _reproduction = hr.getReproduction();
                if (_reproduction.getOrder() == null) // als er geen order is, dan samenvoegen
                    _reproductions.add(_reproduction);
            }

            if ( _reproductions.size()>1) {
                final Reproduction reproductionClone = (Reproduction) new Reproduction().mergeWith(_reproductions.iterator().next()); // willekeur. Dit is de basis voor de nieuwe reproductie.

                for (HoldingReproduction hr : hrs) { // pak alle holders en schuif die onder de nieuwe)
                    final HoldingReproduction holdingReproductionClone = new HoldingReproduction();
                    holdingReproductionClone.mergeWith(hr);
                    holdingReproductionClone.setReproduction(reproductionClone);
                    reproductionClone.getHoldingReproductions().add(holdingReproductionClone);
                }

                // Sla de nieuwe reproductie op
                reproductionService.saveReproduction(reproductionClone);

                // Verwijder alle reproducties (en daarmee alle holdings wegens cascade delete)
                for (Reproduction _reproduction : _reproductions) {
                    _reproduction.getHoldingReproductions().clear();
                    reproductionService.removeReproduction(_reproduction);
                }
            }
        }

        return "redirect:/reproduction/" + qs;
    }

    /**
     * Show print marked holdings (except already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reproductions.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "print")
    public String batchProcessPrint(HttpServletRequest req, @RequestParam(required = false) List<String> checked) {
        List<HoldingReproduction> hrs = getHoldingReproductionsForBulk(checked);
        if (!hrs.isEmpty()) {
            try {
                reproductionService.printItems(hrs, false);
            } catch (PrinterException e) {
                return "reproduction_print_failure";
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
    }

    /**
     * Show print marked holdings (including already printed).
     *
     * @param req     The HTTP request object.
     * @param checked The marked reproductions.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "printForce")
    public String batchProcessPrintForce(HttpServletRequest req, @RequestParam(required = false) List<String> checked) {
        List<HoldingReproduction> hrs = getHoldingReproductionsForBulk(checked);
        if (!hrs.isEmpty()) {
            try {
                reproductionService.printItems(hrs, true);
            } catch (PrinterException e) {
                return "reproduction_print_failure";
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
    }

    /**
     * Change status of marked reproductions.
     *
     * @param req       The HTTP request object.
     * @param checked   The reproductions marked.
     * @param newStatus The status the selected reproductions should be set to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/batchprocess", method = RequestMethod.POST, params = "changeStatus")
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String batchProcessChangeStatus(HttpServletRequest req, @RequestParam(required = false) List<String> checked,
                                           @RequestParam Reproduction.Status newStatus) {
        for (Integer requestId : getRequestIdsFromBulk(checked)) {
            Reproduction r = reproductionService.getReproductionById(requestId);

            // Only change reproductions which exist
            if (r != null) {
                reproductionService.updateStatusAndAssociatedHoldingStatus(r, newStatus);
                reproductionService.saveReproduction(r);
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
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
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String batchProcessChangeHoldingStatus(HttpServletRequest req,
                                                  @RequestParam(required = false) List<String> checked,
                                                  @RequestParam Holding.Status newHoldingStatus) {
        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Holding h = recordService.getHoldingById(bulkActionIds.getHoldingId());
            if (h != null) {
                // Only update the status if the holding is active for the same reproduction
                Request request = generalRequestService.getActiveFor(h);
                if ((request instanceof Reproduction) &&
                        (((Reproduction) request).getId() == bulkActionIds.getRequestId())) {
                    // Set the new status
                    recordService.updateHoldingStatus(h, newHoldingStatus);
                    recordService.saveHolding(h);
                }
            }
        }

        String qs = (req.getQueryString() != null) ? "?" + req.getQueryString() : "";
        return "redirect:/reproduction/" + qs;
    }

    /**
     * Get marked holding reproductions.
     *
     * @param checked A list of request id and holding id pairs.
     * @return The holding reproductions.
     */
    private List<HoldingReproduction> getHoldingReproductionsForBulk(List<String> checked) {
        List<HoldingReproduction> hrs = new ArrayList<>();
        for (BulkActionIds bulkActionIds : getIdsFromBulk(checked)) {
            Reproduction r = reproductionService.getReproductionById(bulkActionIds.getRequestId());
            for (HoldingReproduction hr : r.getHoldingReproductions()) {
                if (hr.getHolding().getId() == bulkActionIds.getHoldingId())
                    hrs.add(hr);
            }
        }
        return hrs;
    }

    /**
     * Show the create form of a reproduction.
     *
     * @param req   The HTTP request.
     * @param path  The pid/signature string (URL encoded).
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}", method = RequestMethod.GET)
    public String showCreateForm(HttpServletRequest req, @PathVariable String path, Model model) {
        Reproduction reproduction = new Reproduction();
        reproduction.setHoldingReproductions(uriPathToHoldingReproductions(path));
        return processReproductionCreation(req, reproduction, null, model, false);
    }

    /**
     * Process the create form of a reproduction.
     *
     * @param req    The HTTP request.
     * @param newRep The submitted reproduction.
     * @param result The binding result to put errors in.
     * @param model  The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/createform/{path:.*}", method = RequestMethod.POST)
    public String processCreateForm(HttpServletRequest req, @ModelAttribute("reproduction") Reproduction newRep,
                                    BindingResult result, Model model) {
        return processReproductionCreation(req, newRep, result, model, true);
    }

    /**
     * Translates the URI path to a list of holding reproductions.
     *
     * @param path The given path.
     * @return A list of created holding reproductions.
     */
    private List<HoldingReproduction> uriPathToHoldingReproductions(String path) {
        List<Holding> holdings = uriPathToHoldings(path);
        if (holdings == null)
            return null;

        List<HoldingReproduction> hrs = new ArrayList<>();
        for (Holding holding : holdings) {
            HoldingReproduction hr = new HoldingReproduction();
            hr.setHolding(holding);
            hrs.add(hr);
        }
        return hrs;
    }

    /**
     * Processes the reproduction creation procedure.
     *
     * @param req          The request.
     * @param reproduction The reproduction to create.
     * @param result       The binding result.
     * @param model        The model.
     * @param commit       Whether to commit the result to the database.
     * @return The view to resolve.
     */
    private String processReproductionCreation(HttpServletRequest req, Reproduction reproduction, BindingResult result,
                                               Model model, boolean commit) {
        if (hasNoAvailableHoldings(model, reproduction))
            return "reproduction_error";

        // Add all the standard reproduction options and custom notes to the model
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                getStandardReproductionOptions(reproduction.getHoldings());
        Map<String, List<ReproductionStandardOption>> unavailableStandardOptions =
                getStandardOptionsNotAvailable(reproduction.getHoldings(), reproductionStandardOptions);

        model.addAttribute("reproductionStandardOptions", reproductionStandardOptions);
        model.addAttribute("unavailableStandardOptions", unavailableStandardOptions);
        model.addAttribute("reproductionCustomNotes", reproductionService.getAllReproductionCustomNotesAsMap());

        // For new reproduction requests, select the first available option for each holding in the request
        if (!commit)
            autoSelectFirstAvailableOption(reproduction, reproductionStandardOptions, unavailableStandardOptions);

        try {
            if (commit) {
                determineHumanService.checkCaptcha(req, result, model); // Make sure a Captcha was entered correctly
                reproductionService.createOrEdit(reproduction, null, result, true);
                if (!result.hasErrors() && !reproduction.getHoldingReproductions().isEmpty()) {
                    autoPrintReproduction(reproduction);
                    return determineNextStep(reproduction, model);
                }
            } else {
                reproductionService.validateReproductionHoldings(reproduction, null);
            }
        } catch (NoHoldingsException e) {
            throw new ResourceNotFoundException();
        } catch (ClosedException e) {
            model.addAttribute("error", "restricted");
            return "reproduction_error";
        } catch (ClosedForReproductionException e) {
            model.addAttribute("error", "closed");
            return "reproduction_error";
        }

        // If there are suddenly no holding reproductions left, apparently nothing was available
        if (reproduction.getHoldingReproductions().isEmpty()) {
            model.addAttribute("error", "nothingAvailable");
            return "reproduction_error";
        }

        model.addAttribute("reproduction", reproduction);
        return "reproduction_create";
    }

    /**
     * Returns a map of the possible standard reproduction options per holding signature.
     *
     * @param holdings The holdings.
     * @return A map with options per holding.
     */
    private Map<String, List<ReproductionStandardOption>> getStandardReproductionOptions(List<Holding> holdings) {
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                new HashMap<>();
        List<ReproductionStandardOption> standardOptions = reproductionService.getAllReproductionStandardOptions();

        for (Holding holding : holdings) {
            List<ReproductionStandardOption> standardOptionsForHolding = new ArrayList<>();
            if (!holding.allowOnlyCustomReproduction()) {
                for (ReproductionStandardOption standardOption : standardOptions) {
                    if (standardOption.isEnabled() &&
                            reproductionService.recordAcceptsReproductionOption(holding.getRecord(), standardOption))
                        standardOptionsForHolding.add(standardOption);
                }

                List<ReproductionStandardOption> inSorOnlyCustomForHolding =
                        reproductionService.getStandardOptionsInSorOnlyCustom(holding, standardOptionsForHolding);
                standardOptionsForHolding.removeAll(inSorOnlyCustomForHolding);
            }
            reproductionStandardOptions.put(holding.getSignature(), standardOptionsForHolding);
        }

        return reproductionStandardOptions;
    }

    /**
     * Returns a map of the unavailable standard reproduction options per holding signature from the given holdings.
     *
     * @param holdings        The holdings.
     * @param standardOptions The possible standard reproduction options per holding signature.
     * @return A map with options per holding.
     */
    private Map<String, List<ReproductionStandardOption>> getStandardOptionsNotAvailable(List<Holding> holdings,
                                                                                         Map<String, List<ReproductionStandardOption>> standardOptions) {
        Map<String, List<ReproductionStandardOption>> unavailableStandardOptions =
                new HashMap<>();

        for (Holding holding : holdings) {
            List<ReproductionStandardOption> unavailableForHolding = new ArrayList<>();
            if (holding.getStatus() != Holding.Status.AVAILABLE) {
                unavailableForHolding =
                        reproductionService.getStandardOptionsNotInSor(holding, standardOptions.get(holding.getSignature()));
            }
            unavailableStandardOptions.put(holding.getSignature(), unavailableForHolding);
        }

        return unavailableStandardOptions;
    }

    /**
     * Returns a map of only the available standard reproduction options per holding signature from the given holdings.
     *
     * @param holdings The holdings.
     * @return A map with options per holding.
     */
    private Map<String, List<ReproductionStandardOption>> getStandardOptionsAvailable(List<Holding> holdings) {
        Map<String, List<ReproductionStandardOption>> availableStandardOptions =
                new HashMap<>();
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                getStandardReproductionOptions(holdings);
        Map<String, List<ReproductionStandardOption>> unavailableStandardOptions =
                getStandardOptionsNotAvailable(holdings, reproductionStandardOptions);

        for (Holding h : holdings) {
            List<ReproductionStandardOption> standardOptions =
                    new ArrayList<>(reproductionStandardOptions.get(h.getSignature()));
            standardOptions.removeAll(unavailableStandardOptions.get(h.getSignature()));
            availableStandardOptions.put(h.getSignature(), standardOptions);
        }

        return availableStandardOptions;
    }

    /**
     * Auto select the first available standard option for each holding, if any standard options are available.
     *
     * @param reproduction               The reproduction request.
     * @param standardOptions            The standard options.
     * @param unavailableStandardOptions The standard options which are not available.
     */
    private void autoSelectFirstAvailableOption(Reproduction reproduction,
                                                Map<String, List<ReproductionStandardOption>> standardOptions,
                                                Map<String, List<ReproductionStandardOption>> unavailableStandardOptions) {
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            List<ReproductionStandardOption> availableOptions =
                    new ArrayList<>(standardOptions.get(hr.getHolding().getSignature()));
            availableOptions.removeAll(unavailableStandardOptions.get(hr.getHolding().getSignature()));

            if (!availableOptions.isEmpty())
                hr.setStandardOption(availableOptions.getFirst());
        }
    }

    /**
     * Auto print all holdings of the given reproduction, if possible.
     * <p/>
     * Run this in a separate thread, we do nothing on failure so in this case this is perfectly possible.
     *
     * @param reproduction The reproduction.
     */
    @Async
    protected void autoPrintReproduction(final Reproduction reproduction) {
        try {
            reproductionService.printReproduction(reproduction);
        } catch (PrinterException e) {
            // Do nothing, let an employee print it later on
            LOGGER.warn("Printing reproduction failed", e);
        }
    }

    /**
     * After creating a new reproduction, determine the next step.
     * Either the reproduction has to go to the reading room first,
     * or an offer can be created right away allowing the customer to confirm/pay immediately.
     *
     * @param reproduction The reproduction.
     * @param model        The model.
     * @return The view to resolve.
     */
    private String determineNextStep(Reproduction reproduction, Model model) {
        model.asMap().clear();

        if (reproduction.getStatus() == Reproduction.Status.HAS_ORDER_DETAILS) {
            // Mail the confirmation (offer is ready) to the customer
            try {
                reproductionMailer.mailOfferReady(reproduction);
            } catch (MailException me) {
                model.addAttribute("error", "mail");
            }

            return "redirect:/reproduction/confirm/" + reproduction.getId() + "/" + reproduction.getToken();
        } else {
            // Mail the reproduction pending details to the customer and inform the reading room
            try {
                reproductionMailer.mailPending(reproduction);
            } catch (MailException me) {
                model.addAttribute("error", "mail");
            }

            model.addAttribute("reproduction", reproduction);

            return "reproduction_pending";
        }
    }

    /**
     * Checks the holdings of a request.
     *
     * @param model   The model to add errors to.
     * @param request The Request with holdings to check.
     * @return Whether no errors were found.
     */
    @Override
    protected boolean hasNoAvailableHoldings(Model model, Request request) {
        if (super.hasNoAvailableHoldings(model, request)) {
            return true;
        }

        // Determine whether a record is closed for reproduction
        for (HoldingRequest holdingRequest : request.getHoldingRequests()) {
            if (!holdingRequest.getHolding().getRecord().isOpenForReproduction()) {
                model.addAttribute("error", "closed");
                return true;
            }
        }

        return false;
    }

    /**
     * Show the confirmation form of a reproduction.
     *
     * @param reproductionId The id of the reproduction.
     * @param token          A token to prevent unauthorized access to the reproduction.
     * @param model          The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/confirm/{reproductionId:[\\d]+}/{token}", method = RequestMethod.GET)
    public String showConfirm(@PathVariable int reproductionId, @PathVariable String token, Model model) {
        Reproduction reproduction = reproductionService.getReproductionById(reproductionId);
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
        Reproduction reproduction = reproductionService.getReproductionById(reproductionId);
        validateToken(reproduction, token);
        return processConfirmation(req, reproduction, model, true);
    }

    /**
     * Processes the confirmation of a reproduction by the customer.
     *
     * @param req          The HTTP request.
     * @param reproduction The reproduction.
     * @param model        The model to add response attributes to.
     * @param commit       Whether to commit the confirmation to the database and create an order.
     * @return The view to resolve.
     */
    private String processConfirmation(HttpServletRequest req, Reproduction reproduction, Model model, boolean commit) {
        if (reproduction == null)
            throw new InvalidRequestException("No such reproduction.");

        if (reproduction.getStatus().compareTo(Reproduction.Status.HAS_ORDER_DETAILS) < 0)
            throw new InvalidRequestException("Reproduction does not have all of the order details yet.");

        // If the customer already confirmed the reproduction, just redirect to the payment page
        if (reproduction.getStatus() == Reproduction.Status.CONFIRMED) {
            try {
                Order order = reproduction.getOrder();
                if (order == null) {
                    order = reproductionService.createOrder(reproduction);

                    // If the reproduction is for free, take care of delivery
                    if (reproduction.isForFree()) {
                        // Determine if we can move up to either 'completed' or 'active' immediately
                        changeStatusAfterPayment(reproduction);

                        // Show payment accepted page
                        return "redirect:/reproduction/order/confirm";
                    }
                }

                return "redirect:" + order.getCheckoutUrl();
            } catch (IncompleteOrderDetailsException onre) {
                // We already checked for this one though
                throw new InvalidRequestException("Reproduction is not ready yet.");
            } catch (OrderRegistrationFailureException orfe) {
                String msg = messageSource.getMessage("payment.error", null, LocaleContextHolder.getLocale());
                throw new InvalidRequestException(msg);
            }
        }

        // If already moved on from the status 'confirmed', the customer has no business on this page anymore
        if (reproduction.getStatus().compareTo(Reproduction.Status.CONFIRMED) >= 0)
            throw new InvalidRequestException("Reproduction has been confirmed already.");

        model.addAttribute("reproduction", reproduction);
        if (commit) {
            // Did the customer accept the terms and conditions?
            String accept = req.getParameter("accept_terms_conditions");
            if (!"accept".equals(accept)) {
                String msg = messageSource.getMessage("accept.error", null, LocaleContextHolder.getLocale());
                model.addAttribute("acceptError", msg);
                return "reproduction_confirm";
            }

            try {
                // Change status to 'confirmed by customer' and create order
                reproductionService.updateStatusAndAssociatedHoldingStatus(reproduction, Reproduction.Status.CONFIRMED);
                Order order = reproductionService.createOrder(reproduction);

                // If the reproduction is for free, take care of delivery
                if (reproduction.isForFree()) {
                    // Determine if we can move up to either 'completed' or 'active' immediately
                    changeStatusAfterPayment(reproduction);

                    // Show payment accepted page
                    return "redirect:/reproduction/order/confirm";
                }

                // Otherwise redirect the user to the payment page
                return "redirect:" + order.getCheckoutUrl();
            } catch (IncompleteOrderDetailsException onre) {
                // We already checked for this one though
                throw new InvalidRequestException("Reproduction is not ready yet.");
            } catch (OrderRegistrationFailureException orfe) {
                String msg = messageSource.getMessage("payment.error", null, LocaleContextHolder.getLocale());
                model.addAttribute("paymentError", msg);
            }
        }

        return "reproduction_confirm";
    }

    /**
     * Reproduction confirmed, no payment required.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/confirm", method = RequestMethod.GET)
    public String confirm() {
        return "reproduction_order_confirm";
    }

    /**
     * Payment response.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/redirect", method = RequestMethod.GET)
    public String accept() {
        return "reproduction_order_redirect";
    }

    /**
     * Payment response, payment was canceled.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/order/cancel", method = RequestMethod.GET)
    public String cancel() {
        return "reproduction_order_cancel";
    }

    /**
     * The payment web hook
     */
    @RequestMapping(value = "/order/webhook", method = RequestMethod.POST)
    public ResponseEntity<String> webhook(@RequestParam String id) {
        LOGGER.debug("/reproduction/order/webhook : Mollie called webhook with payment id {}", id);

        // Check the order ...
        Order order = reproductionService.getOrderById(id);
        if (order == null) {
            LOGGER.error("/reproduction/order : Order not found for payment id {}", id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // ... and the reproduction
        Reproduction reproduction = order.getReproduction();
        if (reproduction == null) {
            LOGGER.error("/reproduction/order : Reproduction not found for order with payment id {}", id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Everything is fine, refresh the order
        Order refreshedOrder = reproductionService.refreshOrder(order);
        LOGGER.info("Order refreshed: {}", refreshedOrder);

        // If the order is paid, update the reproduction status
        if (order.getPayed() == Order.ORDER_PAYED) {
            changeStatusAfterPayment(reproduction);
            reproductionService.saveReproduction(reproduction);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Determine if we can move up to either 'completed' or 'active' immediately.
     *
     * @param reproduction The reproduction.
     */
    private void changeStatusAfterPayment(Reproduction reproduction) {
        reproductionService.updateStatusAndAssociatedHoldingStatus(reproduction, Reproduction.Status.ACTIVE);
        if (reproduction.isCompletelyInSor())
            reproductionService.updateStatusAndAssociatedHoldingStatus(reproduction, Reproduction.Status.COMPLETED);
    }

    /**
     * Update a reproduction.
     *
     * @param id    ID of the reproduction to fetch.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/{id:[\\d]+}/edit", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String showEditForm(@PathVariable int id, Model model) {
        Reproduction r = reproductionService.getReproductionById(id);
        if (r == null)
            throw new ResourceNotFoundException();

        // It is not allowed to modify a reproduction after confirmation by the customer
        if (r.getStatus().ordinal() >= Reproduction.Status.CONFIRMED.ordinal()) {
            model.addAttribute("error", "confirmed");
            return "reproduction_error";
        }

        model.addAttribute("original", r);
        model.addAttribute("reproduction", r);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(r.getHoldings()));
        model.addAttribute("emailResponses", createEmailResponse(r));

        return "reproduction_mass_create";
    }

    /**
     * Save the reproduction.
     *
     * @param id           ID of the reproduction to fetch.
     * @param reproduction The reproduction.
     * @param result       The object to save the validation errors.
     * @param mail         Whether or not to mail a reproduction confirmation.
     * @param model        The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/{id:[\\d]+}/edit", method = RequestMethod.POST)
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String processEditForm(@PathVariable int id, @ModelAttribute("reproduction") Reproduction reproduction,
                                  BindingResult result, boolean mail, Model model) {
        Reproduction originalReproduction = reproductionService.getReproductionById(id);
        if (originalReproduction == null)
            throw new ResourceNotFoundException();

        // It is not allowed to modify a reproduction after confirmation by the customer
        if (reproduction.getStatus().ordinal() >= Reproduction.Status.CONFIRMED.ordinal()) {
            model.addAttribute("error", "confirmed");
            return "reproduction_error";
        }

        try {
            reproductionService.createOrEdit(reproduction, originalReproduction, result, false);
            if (!result.hasErrors()) {
                // Mail the confirmation (offer is ready) to the customer
                boolean mailSuccess = true;
                if (mail) {
                    try {
                        reproductionMailer.mailOfferReady(originalReproduction);
                    } catch (MailException me) {
                        mailSuccess = false;
                    }
                }
                return "redirect:/reproduction/" + originalReproduction.getId() + (!mailSuccess ? "?mail=error" : "");
            }
        } catch (ClosedException | ClosedForReproductionException e) {
            String msg = messageSource.getMessage("reproduction.error.restricted", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        } catch (NoHoldingsException e) {
            String msg = messageSource.getMessage("reproduction.error.noHoldings", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        }

        model.addAttribute("original", originalReproduction);
        model.addAttribute("reproduction", reproduction);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(reproduction.getHoldings()));
        model.addAttribute("emailResponses", createEmailResponse(reproduction));

        return "reproduction_mass_create";
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
    @PreAuthorize("hasRole('REPRODUCTION_CREATE')")
    public String showMassCreateForm(@RequestParam(required = false) Integer fromReproductionId, Model model) {
        Reproduction newReproduction = new Reproduction();
        if (fromReproductionId != null) {
            Reproduction fromReproduction = reproductionService.getReproductionById(fromReproductionId);
            if (fromReproduction != null) {
                newReproduction.setCustomerEmail(fromReproduction.getCustomerEmail());
                newReproduction.setCustomerName(fromReproduction.getCustomerName());
            }
        }
        model.addAttribute("reproduction", newReproduction);

        // Add all available standard reproduction options to the model
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                getStandardOptionsAvailable(newReproduction.getHoldings());

        model.addAttribute("reproductionStandardOptions", reproductionStandardOptions);

        return "reproduction_mass_create";
    }

    /**
     * Process the search for new holdings to add to the mass reproduction.
     *
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform", method = RequestMethod.POST, params = {"searchSubmit"})
    @PreAuthorize("hasRole('REPRODUCTION_CREATE')")
    public String processSearchMassCreateForm(@ModelAttribute("reproduction") Reproduction newReproduction,
                                              @RequestParam(required = false) String searchTitle,
                                              @RequestParam(required = false) String searchSignature,
                                              Model model, Pageable pageable) {
        Page<Holding> holdingList = searchMassCreate(newReproduction, searchTitle, searchSignature, pageable);

        model.addAttribute("reproduction", newReproduction);
        model.addAttribute("pagedItem", holdingList);

        List<Holding> holdings = new ArrayList<>();
        holdings.addAll(newReproduction.getHoldings());
        holdings.addAll(holdingList.stream().toList());

        // Add all available standard reproduction options to the model
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                getStandardOptionsAvailable(holdings);

        model.addAttribute("reproductionStandardOptions", reproductionStandardOptions);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(holdings));

        return "reproduction_mass_create_holding";
    }

    /**
     * Save the new mass reproduction.
     *
     * @param newReproduction The already semi-built reproduction.
     * @param result          The object to save the validation errors.
     * @param searchTitle     The keywords to search for in the title.
     * @param searchSignature The keywords to search for in the signature.
     * @param mail            Whether or not to mail a reproduction confirmation.
     * @param model           The model to add attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/masscreateform", method = RequestMethod.POST)
    @PreAuthorize("hasRole('REPRODUCTION_CREATE')")
    public String processMassCreateForm(@ModelAttribute("reproduction") Reproduction newReproduction,
                                        BindingResult result,
                                        @RequestParam(required = false) String searchTitle,
                                        @RequestParam(required = false) String searchSignature,
                                        boolean mail, Model model, Pageable pageable) {

        try {
            reproductionService.createOrEdit(newReproduction, null, result, false);
            if (!result.hasErrors()) {
                reproductionService.autoPrintReproduction(newReproduction);

                // Mail the confirmation (offer is ready) to the customer
                boolean mailSuccess = true;
                if (mail) {
                    try {
                        reproductionMailer.mailOfferReady(newReproduction);
                    } catch (MailException me) {
                        mailSuccess = false;
                    }
                }
                return "redirect:/reproduction/" + newReproduction.getId() + (!mailSuccess ? "?mail=error" : "");
            }
        } catch (ClosedException | ClosedForReproductionException e) {
            String msg = messageSource.getMessage("reproduction.error.restricted", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        } catch (NoHoldingsException e) {
            String msg = messageSource.getMessage("reproduction.error.noHoldings", null, "",
                    LocaleContextHolder.getLocale());
            result.addError(new ObjectError(result.getObjectName(), null, null, msg));
        }

        Page<Holding> holdingList = searchMassCreate(newReproduction, searchTitle, searchSignature, pageable);
        model.addAttribute("reproduction", newReproduction);
        model.addAttribute("pagedItem", holdingList);

        List<Holding> holdings = new ArrayList<>();
        holdings.addAll(newReproduction.getHoldings());
        holdings.addAll(holdingList.stream().toList());

        // Add all available standard reproduction options to the model
        Map<String, List<ReproductionStandardOption>> reproductionStandardOptions =
                getStandardOptionsAvailable(holdings);

        model.addAttribute("reproductionStandardOptions", reproductionStandardOptions);
        model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(holdings));

        return "reproduction_mass_create";
    }

    /**
     * Creates an email response for custom reproductions.
     *
     * @param reproduction The reproduction.
     * @return A map with the email responses for the custom reproductions.
     */
    private Map<String, String> createEmailResponse(Reproduction reproduction) {
        Map<String, String> emailResponses = new HashMap<>();
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            if ((hr.getCustomReproductionCustomer() != null) && !hr.getCustomReproductionCustomer().isEmpty()) {
                String response = reproduction.getCustomerName() + "\n";
                response += messageSource.getMessage("reproductionMail.reproductionId", null, "", reproduction.getRequestLocale());
                response += ": " + reproduction.getId() + "\n\n";
                response += messageSource.getMessage("record.title", null, "", reproduction.getRequestLocale());
                response += ": " + hr.getHolding().getRecord().getTitle() + "\n";
                response += messageSource.getMessage("record.externalInfo.author", null, "", reproduction.getRequestLocale());
                response += ": " + hr.getHolding().getRecord().getExternalInfo().getAuthor() + "\n\n";
                response += messageSource.getMessage("reproduction.customReproductionCustomer", null, "", reproduction.getRequestLocale());
                response += ":\n" + hr.getCustomReproductionCustomer();

                emailResponses.put(String.valueOf(hr.getId()), response.trim());
            }
        }
        return emailResponses;
    }

    /**
     * Displays all standard reproduction options for editing.
     *
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/standardoptions", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String showStandardOptions(Model model) {
        ReproductionStandardOptions standardOptions = new ReproductionStandardOptions(
                reproductionService.getAllReproductionStandardOptions(), reproductionService.getAllReproductionCustomNotes());
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
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY')")
    public String editStandardOptions(@ModelAttribute("standardOptions") ReproductionStandardOptions standardOptions,
                                      BindingResult result, Model model) {
        reproductionService.editStandardOptions(standardOptions, result);
        model.addAttribute("standardOptions", standardOptions);
        return "reproduction_standard_options_edit";
    }

    /**
     * Get a list with the amount payed for reproductions per day.
     *
     * @param req   The HTTP request object.
     * @param model Passed view model.
     * @return The name of the view to use.
     */
    @RequestMapping(value = "/materials", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_VIEW')")
    public String reproductionMaterials(HttpServletRequest req, Model model) {
        Map<String, String[]> p = req.getParameterMap();

        CriteriaBuilder cbMaterial = reproductionService.getHoldingReproductionCriteriaBuilder();
        ReproductionMaterialStatistics materialStatistics = new ReproductionMaterialStatistics(cbMaterial, p);
        CriteriaQuery<Tuple> cqMaterials = materialStatistics.tuple();

        CriteriaBuilder cbPayment = reproductionService.getHoldingReproductionCriteriaBuilder();
        ReproductionPaymentStatistics paymentStatistics = new ReproductionPaymentStatistics(cbPayment, p);
        CriteriaQuery<Tuple> cqPayments = paymentStatistics.tuple();

        model.addAttribute("tuplesMaterials", reproductionService.listTuples(cqMaterials));
        model.addAttribute("tuplePayedAmounts", reproductionService.listTuples(cqPayments));

        return "reproduction_materials";
    }

    /**
     * Get an Excel download with the payed reproductions per for the given time period.
     *
     * @param req The HTTP request object.
     * @param res The HTTP response object.
     */
    @RequestMapping(value = "/excel", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_VIEW')")
    public void reproductionMaterials(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, String[]> p = req.getParameterMap();

        Date from = getFromDateFilter(p);
        from = (from != null) ? from : new Date();
        Date to = getToDateFilter(p);
        to = (to != null) ? to : new Date();

        List<Reproduction> payedReproductions = getPayedReproductions(from, to);
        ReproductionExcel reproductionExcel = new ReproductionExcel(payedReproductions, messageSource);

        res.setContentType("application/vnd.ms-excel");
        res.setHeader("Content-Disposition", "attachment;filename=reproductions.xls");
        reproductionExcel.writeToStream(res.getOutputStream());
        res.flushBuffer();
    }

    @RequestMapping(value = "/{id:[\\d]+}/convert", method = RequestMethod.GET)
    @PreAuthorize("hasRole('REPRODUCTION_MODIFY') && hasRole('RESERVATION_CREATE')")
    public String convert(@PathVariable int id) {
        final Reproduction reproduction = reproductionService.getReproductionById(id);
        if (reproduction == null)
            throw new ResourceNotFoundException();

        final Reservation reservation = new Reservation();
        reservation.mergeWith(reproduction);
        for (HoldingReproduction holdingReproduction : reproduction.getHoldingReproductions()) {
            final HoldingReservation holdingReservation = new HoldingReservation();
            holdingReservation.mergeWith(holdingReproduction);
            holdingReservation.setReservation(reservation);
            reservation.getHoldingReservations().add(holdingReservation);
        }

        reproduction.getHoldingReproductions().clear();
        final Reservation saved = reservationService.saveReservation(reservation);
        reproductionService.removeReproduction(reproduction);

        return "redirect:/reservation/" + saved.getId();
    }

    /**
     * Returns the payed reproductions for a given period.
     *
     * @param from From date.
     * @param to   To date.
     * @return The paid reproductions.
     */
    private List<Reproduction> getPayedReproductions(Date from, Date to) {
        CriteriaBuilder cb = reproductionService.getHoldingReproductionCriteriaBuilder();
        CriteriaQuery<Reproduction> query = cb.createQuery(Reproduction.class);

        // Join all required tables
        Root<Reproduction> repRoot = query.from(Reproduction.class);
        repRoot.fetch("holdingReproductions", JoinType.LEFT);

        // Within the selected date range
        Expression<Date> reproductionDate = repRoot.get("datePaymentAccepted");
        Expression<Boolean> fromExpr = cb.greaterThanOrEqualTo(reproductionDate, from);
        Expression<Boolean> toExpr = cb.lessThanOrEqualTo(reproductionDate, to);

        // And only active or completed reproductions
        Expression<Reproduction.Status> status = repRoot.get("status");
        Expression<Boolean> statusExpr = cb.in(status)
                .value(Reproduction.Status.ACTIVE)
                .value(Reproduction.Status.COMPLETED)
                .value(Reproduction.Status.DELIVERED);

        query.where(cb.and(statusExpr, cb.and(fromExpr, toExpr)));

        return new ArrayList<>(new LinkedHashSet<>(reproductionService.listReproductions(query)));
    }

    /**
     * Validates the token bound to a reproduction.
     *
     * @param reproduction The reproduction.
     * @param token        The token.
     */
    private void validateToken(Reproduction reproduction, String token) {
        if ((reproduction != null) && !reproduction.getToken().equalsIgnoreCase(token)) {
            throw new InvalidRequestException("Invalid token provided.");
        }
    }
}
