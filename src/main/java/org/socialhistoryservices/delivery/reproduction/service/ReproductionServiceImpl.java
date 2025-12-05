package org.socialhistoryservices.delivery.reproduction.service;

import com.mollie.mollie.models.components.PaymentResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.socialhistoryservices.delivery.api.*;
import org.socialhistoryservices.delivery.config.PrinterConfiguration;
import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.reproduction.dao.*;
import org.socialhistoryservices.delivery.reproduction.entity.*;
import org.socialhistoryservices.delivery.reproduction.util.BigDecimalUtils;
import org.socialhistoryservices.delivery.reproduction.util.Copies;
import org.socialhistoryservices.delivery.reproduction.util.Pages;
import org.socialhistoryservices.delivery.reproduction.util.ReproductionStandardOptions;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.request.service.AbstractRequestService;
import org.socialhistoryservices.delivery.request.service.ClosedException;
import org.socialhistoryservices.delivery.request.service.NoHoldingsException;
import org.socialhistoryservices.delivery.request.service.RequestPrintable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.*;

/**
 * Represents the service of the reproduction package.
 */
@Service
@Transactional
public class ReproductionServiceImpl extends AbstractRequestService implements ReproductionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReproductionServiceImpl.class);

    @Autowired
    private ReproductionDAO reproductionDAO;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private HoldingReproductionDAO holdingReproductionDAO;

    @Autowired
    private ReproductionStandardOptionDAO reproductionStandardOptionDAO;

    @Autowired
    private ReproductionCustomNoteDAO reproductionCustomNoteDAO;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SharedObjectRepositoryService sorService;

    @Autowired
    private ReproductionMailer reproductionMailer;

    @Autowired
    private BeanFactory bf;

    @Autowired
    private PrinterConfiguration printerConfiguration;

    /**
     * Add a Reproduction to the database.
     *
     * @param obj Reproduction to add.
     */
    public void addReproduction(Reproduction obj) {
        // Make sure the holdings get set to the correct status.
        updateStatusAndAssociatedHoldingStatus(obj, obj.getStatus());

        // Add to the database
        reproductionDAO.add(obj);
    }

    /**
     * Remove a Reproduction from the database.
     *
     * @param reproduction Reproduction to remove.
     */
    public void removeReproduction(Reproduction reproduction) {
        // Set all holdings linked to this reproduction back to AVAILABLE.
        // Note that we are in a transaction here, so it does not matter the records are still linked
        // to the reproduction when setting them to available.
        changeHoldingStatus(reproduction, Holding.Status.AVAILABLE);
        reproductionDAO.remove(reproduction);
    }

    /**
     * Change the status of all holdings in a reproduction.
     *
     * @param reproduction Reproduction to change status for.
     * @param status       Status to change holdings to.
     */
    public void changeHoldingStatus(Reproduction reproduction, Holding.Status status) {
        super.changeHoldingStatus(reproduction, status);
        saveReproduction(reproduction);
    }

    /**
     * Save changes to a Reproduction in the database.
     *
     * @param obj Reproduction to save.
     */
    public Reproduction saveReproduction(Reproduction obj) {
        return reproductionDAO.save(obj);
    }

    /**
     * Add/update the standard reproduction options.
     *
     * @param standardOptions The standard reproduction options.
     */
    private void addOrUpdateStandardOptions(ReproductionStandardOptions standardOptions) {
        for (ReproductionStandardOption option1 : standardOptions.getOptions()) {
            boolean has = false;
            for (ReproductionStandardOption option2 : getAllReproductionStandardOptions()) {
                if (option1.getId() == option2.getId()) {
                    option2.mergeWith(option1);
                    reproductionStandardOptionDAO.save(option2);
                    has = true;
                }
            }

            if (!has)
                reproductionStandardOptionDAO.add(option1);
        }

        for (ReproductionCustomNote customNote : standardOptions.getCustomNotes()) {
            if (customNote.getId() > 0)
                reproductionCustomNoteDAO.save(customNote);
            else
                reproductionCustomNoteDAO.add(customNote);
        }
    }

    /**
     * Retrieve the Reproduction matching the given id.
     *
     * @param id Id of the Reproduction to retrieve.
     * @return The Reproduction matching the id.
     */
    public Reproduction getReproductionById(int id) {
        return reproductionDAO.getById(id);
    }

    /**
     * Retrieve the Order matching the given id.
     *
     * @param id Id of the Order to retrieve.
     * @return The Order matching the id.
     */
    public Order getOrderById(String id) {
        return orderDAO.getById(id);
    }

    /**
     * Retrieve the ReproductionStandardOption matching the given id.
     *
     * @param id Id of the ReproductionStandardOption to retrieve.
     * @return The ReproductionStandardOption matching the id.
     */
    public ReproductionStandardOption getReproductionStandardOptionById(int id) {
        return reproductionStandardOptionDAO.getById(id);
    }

    /**
     * Get the first Reproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return The first matching Reproduction.
     */
    public Reproduction getReproduction(CriteriaQuery<Reproduction> q) {
        return reproductionDAO.get(q);
    }

    /**
     * List all Reproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reproductions.
     */
    public List<Reproduction> listReproductions(CriteriaQuery<Reproduction> q) {
        return reproductionDAO.list(q);
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    public List<Tuple> listTuples(CriteriaQuery<Tuple> q) {
        return reproductionDAO.listForTuple(q);
    }

    /**
     * List all HoldingReproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> listHoldingReproductions(CriteriaQuery<HoldingReproduction> q) {
        return holdingReproductionDAO.list(q);
    }

    /**
     * List all HoldingReproduction matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> listHoldingReproductions(CriteriaQuery<HoldingReproduction> q,
                                                              int firstResult, int maxResults) {
        return holdingReproductionDAO.list(q, firstResult, maxResults);
    }

    /**
     * Count all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A count of matching HoldingReproductions.
     */
    public long countHoldingReproductions(CriteriaQuery<Long> q) {
        return holdingReproductionDAO.count(q);
    }

    /**
     * Returns all standard options for reproductions.
     *
     * @return A list with all standard options for reproductions.
     */
    public List<ReproductionStandardOption> getAllReproductionStandardOptions() {
        return reproductionStandardOptionDAO.listAll();
    }

    /**
     * Returns all custom notes for reproductions.
     *
     * @return A list with all custom notes for reproductions.
     */
    public List<ReproductionCustomNote> getAllReproductionCustomNotes() {
        return reproductionCustomNoteDAO.listAll();
    }

    /**
     * Returns all custom notes for reproductions.
     *
     * @return A map with all custom notes for reproductions by material type name.
     */
    public Map<String, ReproductionCustomNote> getAllReproductionCustomNotesAsMap() {
        Map<String, ReproductionCustomNote> customNotes = new HashMap<>();
        for (ReproductionCustomNote reproductionCustomNote : getAllReproductionCustomNotes()) {
            customNotes.put(reproductionCustomNote.getMaterialType().name(), reproductionCustomNote);
        }
        return customNotes;
    }

    /**
     * Get a criteria builder for querying Reproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getReproductionCriteriaBuilder() {
        return reproductionDAO.getCriteriaBuilder();
    }

    /**
     * Get a criteria builder for querying HoldingReproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getHoldingReproductionCriteriaBuilder() {
        return holdingReproductionDAO.getCriteriaBuilder();
    }

    /**
     * Mark a specific item in a reproduction as seen, bumping it to the next status.
     *
     * @param r Reproduction to change status for.
     * @param h Holding to bump.
     */
    public void markItem(Reproduction r, Holding h) {
        // Ignore old reproductions
        if (r == null)
            return;

        super.markItem(h);
        markReproduction(r);
        saveReproduction(r);
    }

    /**
     * Mark a reproduction, bumping it to the next status.
     *
     * @param r Reproduction to change status for.
     */
    private void markReproduction(Reproduction r) {
        boolean complete = true;
        for (HoldingReproduction hr : r.getHoldingReproductions()) {
            if (!hr.isCompleted()) {
                Holding holding = hr.getHolding();
                if (holding.getStatus() == Holding.Status.AVAILABLE)
                    hr.setCompleted(true);
                else
                    complete = false;
            }
        }

        if (complete)
            updateStatusAndAssociatedHoldingStatus(r, Reproduction.Status.COMPLETED);
    }

    /**
     * Merge the other reproduction's fields into this reproduction.
     *
     * @param reproduction The reproduction.
     * @param other        The other reproduction.
     */
    public void merge(Reproduction reproduction, Reproduction other) {
        reproduction.setCustomerName(other.getName());
        reproduction.setCustomerEmail(other.getEmail());
        reproduction.setDiscountPercentage(other.getDiscountPercentage());
        reproduction.setAdminstrationCosts(other.getAdminstrationCosts());
        reproduction.setAdminstrationCostsDiscount(other.getAdminstrationCostsDiscount());
        reproduction.setAdminstrationCostsBtwPercentage(other.getAdminstrationCostsBtwPercentage());
        reproduction.setAdminstrationCostsBtwPrice(other.getAdminstrationCostsBtwPrice());
        reproduction.setRequestLocale(other.getRequestLocale());
        reproduction.setDateHasOrderDetails(other.getDateHasOrderDetails());
        reproduction.setComment(other.getComment());

        if (other.getHoldingReproductions() == null) {
            for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
                records.updateHoldingStatus(hr.getHolding(), Holding.Status.AVAILABLE);
            }
            reproduction.setHoldingReproductions(new ArrayList<>());
        }
        else {
            // Delete holdings that were not provided.
            deleteHoldingsNotInProvidedRequest(reproduction, other);

            // Add/update provided.
            addOrUpdateHoldingsProvidedByRequest(reproduction, other);
        }
        updateStatusAndAssociatedHoldingStatus(reproduction, other.getStatus());
    }

    /**
     * Set the reproduction status and update the associated holdings status accordingly.
     * Only updates status forward.
     *
     * @param reproduction The reproduction.
     * @param status       The reproduction which changed status.
     */
    public void updateStatusAndAssociatedHoldingStatus(Reproduction reproduction, Reproduction.Status status) {
        if (status.ordinal() <= reproduction.getStatus().ordinal())
            return;

        reproduction.setStatus(status);

        // Determine actions and specific holding status for the new reproduction status
        boolean completed = false;
        switch (status) {
            case HAS_ORDER_DETAILS:
                reproduction.setDateHasOrderDetails(new Date());
                break;
            case ACTIVE:
                reproduction.setDatePaymentAccepted(new Date());
                mailPayedAndActive(reproduction);
                autoPrintReproduction(reproduction);
                break;
            case DELIVERED:
                completed = true;
                break;
            case CANCELLED:
                completed = true;
                mailCancelled(reproduction);
                // if (reproduction.getOrder() != null)
                    // TODO: refundOrder(reproduction.getOrder()); Find out if and how to do refunds with Mollie
                break;
        }

        // Update the holdings of the reproduction
        if (completed) {
            for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
                if (hr.getHolding().getStatus() == Holding.Status.RESERVED) {
                    hr.setCompleted(true);
                    records.updateHoldingStatus(hr.getHolding(), Holding.Status.AVAILABLE);
                }
            }
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
    public void autoPrintReproduction(final Reproduction reproduction) {
        try {
            printReproduction(reproduction);
        }
        catch (PrinterException e) {
            // Do nothing, let an employee print it later on
            LOGGER.warn("Printing reproduction failed", e);
        }
    }

    /**
     * Email the customer a payment confirmation and email repro concerning the active reproduction.
     *
     * @param reproduction The reproduction.
     */
    private void mailPayedAndActive(Reproduction reproduction) {
        try {
            reproductionMailer.mailPayedAndActive(reproduction);
        }
        catch (MailException me) {
            // Do nothing
        }
    }

    /**
     * Email repro concerning the active reproduction.
     *
     * @param reproduction The reproduction.
     */
    private void mailCancelled(Reproduction reproduction) {
        try {
            reproductionMailer.mailCancelled(reproduction);
        }
        catch (MailException me) {
            // Do nothing
        }
    }

    /**
     * Adds a HoldingRequest to the HoldingRequests assoicated with this request.
     *
     * @param request        The request.
     * @param holdingRequest The HoldingRequests to add.
     */
    protected void addToHoldingRequests(Request request, HoldingRequest holdingRequest) {
        Reproduction reproduction = (Reproduction) request;
        HoldingReproduction holdingReproduction = (HoldingReproduction) holdingRequest;

        holdingReproduction.setReproduction(reproduction);
        reproduction.getHoldingReproductions().add(holdingReproduction);
    }

    /**
     * Prints holding reproductions by using the default printer.
     *
     * @param hrs         The holding reproductions to print.
     * @param alwaysPrint If set to true, already printed reproductions will also be printed.
     * @throws PrinterException Thrown when delivering the print job to the printer failed.
     *                          Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    public void printItems(List<HoldingReproduction> hrs, boolean alwaysPrint) throws PrinterException {
        try {
            Set<Reproduction> reproductions = new HashSet<>();
            List<RequestPrintable> requestPrintables = new ArrayList<>();
            for (HoldingReproduction hr : hrs) {
                // Only print a reproduction if an offer has to be prepared
                // or when the status has changed to ACTIVE and the item is not yet digitally available
                Reproduction.Status status = hr.getReproduction().getStatus();
                if (!hr.hasOrderDetails() || (!hr.isInSor() && (status == Reproduction.Status.ACTIVE))) {
                    ReproductionPrintable rp = new ReproductionPrintable(
                            hr, msgSource, (DateFormat) bf.getBean("dateFormat"), deliveryProperties);
                    requestPrintables.add(rp);
                    reproductions.add(hr.getReproduction());
                }
            }

            printRequest(requestPrintables, printerConfiguration.getPrinterNameArchive(), alwaysPrint);

            for (Reproduction r : reproductions) {
                saveReproduction(r);
            }
        }
        catch (PrinterException e) {
            LOGGER.warn("Printing reproduction failed", e);
            throw e;
        }
    }

    /**
     * Print a (active) reproduction if it was not printed yet.
     *
     * @param reproduction The reproduction to print.
     * @throws PrinterException Thrown when delivering the print job to the printer failed.
     *                          Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    public void printReproduction(Reproduction reproduction) throws PrinterException {
        printItems(reproduction.getHoldingReproductions(), false);
    }

    /**
     * Edit reproductions.
     *
     * @param newReproduction The new reproduction to put in the database.
     * @param oldReproduction The old reproduction in the database (if present).
     * @param result          The binding result object to put the validation errors in.
     * @param isCustomer      Whether the customer is creating the reproduction.
     * @throws ClosedException                Thrown when a holding is provided which
     *                                        references a record which is restrictionType=CLOSED.
     * @throws NoHoldingsException            Thrown when no holdings are provided.
     * @throws ClosedForReproductionException Thrown when a holding is provided which is closed for reproductions.
     */
    public void createOrEdit(Reproduction newReproduction, Reproduction oldReproduction, BindingResult result,
                             boolean isCustomer)
            throws ClosedException, NoHoldingsException, ClosedForReproductionException {
        // Only check for availability on new reproduction requests
        if (oldReproduction == null) {
            // Determine for all the item whether it is already available in the SOR
            for (HoldingReproduction hr : newReproduction.getHoldingReproductions()) {
                hr.setInSor(isHoldingReproductionInSor(hr));
            }

            // Now remove unavailable holdings from the request
            removeUnavailbleHoldings(newReproduction);
        }
        else {
            // Otherwise merge the SOR availability information
            for (HoldingReproduction hrNew : newReproduction.getHoldingReproductions()) {
                for (HoldingReproduction hrOld : oldReproduction.getHoldingReproductions()) {
                    if (hrNew.getHolding().getId() == hrOld.getHolding().getId()) {
                        hrNew.setInSor(hrOld.isInSor());
                    }
                }
            }
        }

        // Only continue if there are any holdings left, otherwise simply remove the reproduction request
        if (!newReproduction.getHoldingReproductions().isEmpty()) {
            // Validate the reproduction
            validateRequest(newReproduction, result);
            validateReproductionHoldings(newReproduction, oldReproduction);

            if (!isCustomer)
                validateReproductionNotCustomer(newReproduction, result);

            // Initialize reproduction and its holdings
            initReproduction(newReproduction, isCustomer && oldReproduction == null);

            // Add or save the record when no errors are present
            if (!result.hasErrors()) {
                // Move status forward if all order details are known
                if (hasOrderDetails(newReproduction)) {
                    newReproduction.setOfferReadyImmediatly(true);
                    updateStatusAndAssociatedHoldingStatus(newReproduction, Reproduction.Status.HAS_ORDER_DETAILS);
                }

                // Reserve all holdings that are not yet in the SOR
                reserveAvailableRequiredHoldings(newReproduction);

                // Now add or save the record
                if (oldReproduction == null) {
                    newReproduction.setCreationDate(new Date());
                    addReproduction(newReproduction);
                }
                else {
                    merge(oldReproduction, newReproduction);
                    saveReproduction(oldReproduction);
                }
            }
        }
        else if (oldReproduction != null) {
            merge(oldReproduction, newReproduction);
            removeReproduction(oldReproduction);
        }
    }

    /**
     * Validate provided holding part of reproduction.
     *
     * @param newRep The new reproduction containing holdings.
     * @param oldRep The old reproduction if applicable (or null).
     * @throws ClosedException                Thrown when a holding is provided which
     *                                        references a record which is restrictionType=CLOSED.
     * @throws NoHoldingsException            Thrown when no holdings are provided.
     * @throws ClosedForReproductionException Thrown when a holding is provided which is closed for reproductions.
     */
    public void validateReproductionHoldings(Reproduction newRep, Reproduction oldRep)
            throws NoHoldingsException, ClosedException, ClosedForReproductionException {
        validateHoldings(newRep, oldRep);

        for (HoldingRequest hr : newRep.getHoldingRequests()) {
            boolean has = false;
            Holding h = hr.getHolding();

            if (oldRep != null) {
                for (HoldingRequest hr2 : oldRep.getHoldingRequests()) {
                    Holding h2 = hr2.getHolding();
                    if (h2.getRecord().equals(h.getRecord()) && h2.getSignature().equals(h.getSignature())) {
                        has = true;
                    }
                }
            }

            // Determine whether the record is closed for reproduction
            if (!has && !h.getRecord().isOpenForReproduction())
                throw new ClosedForReproductionException();
        }
    }

    /**
     * Whether the price and delivery time is determined for all holdings and
     * as a result the reproduction has all the order details.
     *
     * @param reproduction The reproduction.
     * @return Whether all holdings have order details.
     */
    public boolean hasOrderDetails(Reproduction reproduction) {
        List<HoldingReproduction> hrs = reproduction.getHoldingReproductions();
        if ((hrs == null) || hrs.isEmpty())
            return false;

        boolean hasOrderDetails = true;
        for (HoldingReproduction hr : hrs) {
            if (!hr.hasOrderDetails())
                hasOrderDetails = false;
        }

        return hasOrderDetails;
    }

    /**
     * If the holding is not in the SOR and is not available either, remove from request.
     *
     * @param reproduction The reproduction.
     */
    private void removeUnavailbleHoldings(Reproduction reproduction) {
        List<HoldingReproduction> hrToRemove = new ArrayList<>();
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            Holding h = hr.getHolding();
            if (!hr.isInSor() && (h.getStatus() != Holding.Status.AVAILABLE))
                hrToRemove.add(hr);
        }
        reproduction.getHoldingReproductions().removeAll(hrToRemove);
    }

    /**
     * Reserve all the required holdings of the given reproduction that are currently not in the SOR.
     *
     * @param reproduction The reproduction.
     */
    private void reserveAvailableRequiredHoldings(Reproduction reproduction) {
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            if (!hr.isInSor() && (hr.getHolding().getStatus() == Holding.Status.AVAILABLE)) {
                final String parentPid = hr.getHolding().getRecord().getParentPid();
                if (parentPid.endsWith("/" + hr.getHolding().getSignature())) {
                    LOGGER.info("Ignore archival collection level identifier " + parentPid);
                } else {
                    records.updateHoldingStatus(hr.getHolding(), Holding.Status.RESERVED);
                }
            }
        }
    }

    /**
     * Returns standard options for the given holding which are NOT available in the SOR.
     *
     * @param holding                     The holding.
     * @param reproductionStandardOptions The standard options to choose from.
     * @return A list of options that are currently not available online in the SOR.
     */
    public List<ReproductionStandardOption> getStandardOptionsNotInSor(Holding holding,
                                                                       List<ReproductionStandardOption> reproductionStandardOptions) {
        SorMetadata sorMetadata = sorService.getMetadataForPid(holding.determinePid());

        // No metadata means no digital object found in the SOR
        if (sorMetadata == null)
            return reproductionStandardOptions;

        List<ReproductionStandardOption> notInSor = new ArrayList<>();
        for (ReproductionStandardOption standardOption : reproductionStandardOptions) {
            if (!sorMetadataMatchesStandardOption(sorMetadata, standardOption))
                notInSor.add(standardOption);
        }
        return notInSor;
    }

    /**
     * Returns standard options for the given holding which ARE available in the SOR,
     * and therefor does not accept the standard options.
     *
     * @param holding                     The holding.
     * @param reproductionStandardOptions The standard options to choose from.
     * @return A list of options that are currently not available online in the SOR.
     */
    public List<ReproductionStandardOption> getStandardOptionsInSorOnlyCustom(Holding holding,
                                                                              List<ReproductionStandardOption> reproductionStandardOptions) {
        List<ReproductionStandardOption> inSorOnlyCustom = new ArrayList<>();
        if (holding.getRecord().getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.BOOK) {
            SorMetadata sorMetadata = sorService.getMetadataForPid(holding.determinePid());

            // No metadata means no digital object found in the SOR
            if (sorMetadata == null)
                return inSorOnlyCustom;

            for (ReproductionStandardOption standardOption : reproductionStandardOptions) {
                if (sorMetadataMatchesStandardOption(sorMetadata, standardOption))
                    inSorOnlyCustom.add(standardOption);
            }
        }
        return inSorOnlyCustom;
    }

    /**
     * Determine whether a wish for a holding reproduction is in the SOR.
     *
     * @param holdingReproduction The holding reproduction.
     * @return Whether a wish for a holding reproduction is in the SOR.
     */
    private boolean isHoldingReproductionInSor(HoldingReproduction holdingReproduction) {
        ReproductionStandardOption standardOption = holdingReproduction.getStandardOption();
        if (standardOption != null) {
            // Get metadata from the SOR for the specified level
            Holding holding = holdingReproduction.getHolding();
            SorMetadata sorMetadata = sorService.getMetadataForPid(holding.determinePid());
            return sorMetadataMatchesStandardOption(sorMetadata, standardOption);
        }
        return false;
    }

    /**
     * Does the metadata from the SOR match the standard options requirements?
     *
     * @param sorMetadata    The SOR metadata.
     * @param standardOption The standard option to compare against.
     * @return Whether it matches.
     */
    private boolean sorMetadataMatchesStandardOption(SorMetadata sorMetadata,
                                                     ReproductionStandardOption standardOption) {
        // No metadata means no digital object found in the SOR
        if (sorMetadata == null)
            return false;

        // Determine whether the content in the SOR matches the expected content
        ReproductionStandardOption.Level level = standardOption.getLevel();
        String contentType = sorMetadata.getContentType(level.name());
        switch (standardOption.getMaterialType()) {
            case BOOK:
                boolean isPdf = ((contentType != null) && contentType.equals("application/pdf"));
                boolean isPdfMets = (sorMetadata.isMETS() &&
                        (sorMetadata.getFilePids().containsKey("archive pdf")
                                || sorMetadata.getFilePids().containsKey("archive image")));
                return (isPdf || isPdfMets);
            case SOUND:
                String metsAudio = (level == ReproductionStandardOption.Level.MASTER) ? "archive audio" : "reference audio";
                boolean isAudio = ((contentType != null) && contentType.startsWith("audio"));
                boolean isAudioMets = (sorMetadata.isMETS() && sorMetadata.getFilePids().containsKey(metsAudio));
                return (isAudio || isAudioMets);
            case MOVING_VISUAL:
                String metsVideo = (level == ReproductionStandardOption.Level.MASTER) ? "archive video" : "reference video";
                boolean isVideo = ((contentType != null) && contentType.startsWith("video"));
                boolean isVideoMets = (sorMetadata.isMETS() && sorMetadata.getFilePids().containsKey(metsVideo));
                return (isVideo || isVideoMets);
            case VISUAL:
                if (standardOption.getLevel() == ReproductionStandardOption.Level.MASTER) {
                    // Make sure the TIFFs are >= 300 dpi
                    // Due to high possibility of lower resolution TIFFs in the SOR
                    boolean isTiffMets = (sorMetadata.isMETS() && sorMetadata.getFilePids().containsKey("archive image"));
                    return (sorMetadata.isTiff() || isTiffMets);
                }
                else {
                    boolean isJpeg = ((contentType != null) && contentType.equals("image/jpeg"));
                    boolean isJpegMets = (sorMetadata.isMETS() &&
                            sorMetadata.getFilePids().containsKey("hires reference image"));
                    return (isJpeg || isJpegMets);
                }
        }

        return false;
    }

    /**
     * Scheduled task to cancel all reproductions not paid within the time frame after the offer was ready.
     */
    @Scheduled(cron = "0 0 0 * * MON-FRI")
    public void checkPayedReproductions() {
        LOGGER.info("Start run: cancel old unpayed reproductions");

        // Determine the number of days
        int nrOfDays = deliveryProperties.getReproductionMaxDaysPayment();

        // Determine the date that many days ago
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -nrOfDays);

        // Build the query
        CriteriaBuilder builder = getReproductionCriteriaBuilder();
        CriteriaQuery<Reproduction> query = builder.createQuery(Reproduction.class);
        Root<Reproduction> reproductionRoot = query.from(Reproduction.class);
        query.select(reproductionRoot);

        // Only reproductions outside the given time frame
        Expression<Boolean> dateCriteria = builder.lessThan(
                reproductionRoot.get(Reproduction_.dateHasOrderDetails),
                calendar.getTime()
        );

        // Only reproductions that have an offer, but are not yet paid
        Expression<Boolean> statusCriteria = builder.in(reproductionRoot.get(Reproduction_.status))
                .value(Reproduction.Status.HAS_ORDER_DETAILS)
                .value(Reproduction.Status.CONFIRMED);

        query.where(builder.and(dateCriteria, statusCriteria));

        // Cancel all found reproductions
        for (Reproduction reproduction : listReproductions(query)) {
            updateStatusAndAssociatedHoldingStatus(reproduction, Reproduction.Status.CANCELLED);
            saveReproduction(reproduction);
            LOGGER.info("Cancelled unpayed reproduction with id " + reproduction.getId());
        }

        LOGGER.info("Finish run: cancel old unpayed reproductions");
    }

    /**
     * Scheduled task to send a reminder for all reproductions not paid within the time frame after the offer was ready.
     */
    @Scheduled(cron = "0 0 0 * * MON-FRI")
    public void checkReminderReproductions() {
        LOGGER.info("Start run: mail reminder old unpayed reproductions");

        // Determine the number of days
        int nrOfDays = deliveryProperties.getReproductionMaxDaysReminder();

        // Determine the date that many days ago
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -nrOfDays);

        // Build the query
        CriteriaBuilder builder = getReproductionCriteriaBuilder();
        CriteriaQuery<Reproduction> query = builder.createQuery(Reproduction.class);
        Root<Reproduction> reproductionRoot = query.from(Reproduction.class);
        query.select(reproductionRoot);

        // Only reproductions outside the given time frame
        Predicate dateCriteria = builder.lessThan(
                reproductionRoot.get(Reproduction_.dateHasOrderDetails),
                calendar.getTime()
        );

        // Only reproductions that have an offer, but are not yet paid
        Predicate statusCriteria = builder.in(reproductionRoot.get(Reproduction_.status))
                .value(Reproduction.Status.HAS_ORDER_DETAILS)
                .value(Reproduction.Status.CONFIRMED);

        // Only reproductions that have a reminder mail not sent
        Predicate reminderCriteria = builder.equal(reproductionRoot.get(Reproduction_.offerMailReminderSent), false);

        query.where(builder.and(dateCriteria, statusCriteria, reminderCriteria));

        // Send mail for all found reproductions and update mail sent to true
        for (Reproduction reproduction : listReproductions(query)) {
            try {
                reproductionMailer.mailReminder(reproduction);
                reproduction.setOfferMailReminderSent(true);
                saveReproduction(reproduction);
                LOGGER.info("Mailed reminder unpayed reproduction with id " + reproduction.getId());
            }
            catch (MailException me) {
                // Don't do anything... we'll try again tomorrow
                LOGGER.warn("Failed to mail reminder unpayed reproduction with id " + reproduction.getId(), me);
            }
        }

        LOGGER.info("Finish run: mail reminder old unpayed reproductions");
    }

    /**
     * Creates an order for the given reproduction.
     *
     * @param r The reproduction.
     * @return The created and registered order. (Or null if the reproduction is for free)
     * @throws IncompleteOrderDetailsException   Thrown when not all holdings have an order ready.
     * @throws OrderRegistrationFailureException Thrown in case we failed to register the order in Mollie.
     */
    public Order createOrder(Reproduction r) throws IncompleteOrderDetailsException, OrderRegistrationFailureException {
        if (!hasOrderDetails(r))
            throw new IncompleteOrderDetailsException();

        // Check if we maybe already created an order before
        Order order = r.getOrder();
        if (order != null)
            return order;

        if (r.isForFree()) {
            updateStatusAndAssociatedHoldingStatus(r, Reproduction.Status.ACTIVE);
            if (r.isCompletelyInSor())
                updateStatusAndAssociatedHoldingStatus(r, Reproduction.Status.COMPLETED);
        }

        // First attempt to create and register a new order in Mollie
        try {
            PaymentResponse payment = paymentService.createPaymentForReproduction(r);

            order = new Order();
            order.mapFromPayment(payment);

            r.setOrder(order);
            reproductionDAO.save(r);
        }
        catch (PaymentException pe) {
            LOGGER.error("Invalid or no Mollie message received when registering a new order.", pe);
            throw new OrderRegistrationFailureException(pe);
        }

        return order;
    }

    /**
     * Will refresh the given order by retrieving the payment details from Mollie.
     *
     * @param order The order to refresh. The id must be set.
     * @return The refreshed Order when successful.
     */
    public Order refreshOrder(Order order) {
        try {
            PaymentResponse payment = paymentService.getPaymentDetails(order.getId());
            order.mapFromPayment(payment);
            orderDAO.save(order);

            return order;
        }
        catch (PaymentException pe) {
            LOGGER.error(String.format("refreshOrder() : Failed to refresh the order with id %s", order.getId()));
            return null;
        }
    }

    /**
     * Returns whether the record accepts the standard reproduction option.
     *
     * @param record         The record to check.
     * @param standardOption The standard reproduction option.
     * @return Whether the record accepts the given standard reproduction option.
     */
    public boolean recordAcceptsReproductionOption(Record record, ReproductionStandardOption standardOption) {
        // Material types have to match
        if (record.getExternalInfo().getMaterialType() != standardOption.getMaterialType())
            return false;

        // In case of books, the reproduction option is based on the number of pages and the year
        if (record.getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.BOOK) {
            if (!record.getPages().containsNumberOfPages())
                return false;

            Integer year = record.getExternalInfo().getYear();
            if (year != null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -deliveryProperties.getCopyrightYear());
                int acceptedYear = cal.get(Calendar.YEAR);

                return year < acceptedYear;
            }

            return false;
        }

        // In case of visuals, it matters whether it is a poster or not
        if (record.getExternalInfo().getMaterialType() == ExternalRecordInfo.MaterialType.VISUAL) {
            boolean genresContainsPoster = record.getExternalInfo().getGenresSet().contains("poster");
            return standardOption.isPoster() == genresContainsPoster;
        }

        return true;
    }

    /**
     * Validate a request using the provided binding result to store errors.
     *
     * @param request The request.
     * @param result  The binding result.
     */
    protected void validateRequest(Request request, BindingResult result) {
        super.validateRequest(request, result);
        Reproduction reproduction = (Reproduction) request;

        // Validate associated HoldingReproductions if present.
        int i = 0;
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            Holding h = hr.getHolding();
            ReproductionStandardOption standardOption = hr.getStandardOption();

            // If a standard option is chosen, then ignore the provided values
            if (standardOption != null) {
                hr.setPrice(null);
                hr.setNumberOfPages(1);
                hr.setDeliveryTime(null);
                hr.setCustomReproductionCustomer(null);
                hr.setCustomReproductionReply(null);
            }

            // If a custom reproduction, the customer should enter his/her reproduction wishes
            if ((standardOption == null) && (hr.getCustomReproductionCustomer() == null)) {
                result.addError(new FieldError(result.getObjectName(),
                        "holdingReproductions[" + i + "].customReproductionCustomer", "", false,
                        new String[]{"validator.customReproductionCustomer"}, null, "Required"));
            }

            // Determine whether the customer may actually choose the currently chosen standard reproduction option
            if ((standardOption != null) && (!standardOption.isEnabled() || h.allowOnlyCustomReproduction() ||
                    !recordAcceptsReproductionOption(h.getRecord(), standardOption))) {
                result.addError(new FieldError(result.getObjectName(),
                        "holdingReproductions[" + i + "].standardOption", "", false,
                        new String[]{"validator.standardOption"}, null, "Required"));
            }

            i++;
        }
    }

    /**
     * In case the reproduction is not from the customer, then the price and expected delivery time
     * of the custom reproductions have to be given.
     *
     * @param reproduction The reproduction.
     * @param result       The binding result.
     */
    private void validateReproductionNotCustomer(Reproduction reproduction, BindingResult result) {
        // Validate associated HoldingReproductions if present.
        int i = 0;
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            if ((hr.getStandardOption() == null) && ((hr.getPrice() == null)
                    || (hr.getPrice().compareTo(BigDecimal.ZERO) < 0))) {
                result.addError(new FieldError(result.getObjectName(), "holdingReproductions[" + i + "].price",
                        hr.getPrice(), false, new String[]{"validator.price"}, null, "Required"));
            }

            if ((hr.getStandardOption() == null) && ((hr.getDeliveryTime() == null) || (hr.getDeliveryTime() <= 0))) {
                result.addError(new FieldError(result.getObjectName(), "holdingReproductions[" + i + "].deliveryTime",
                        hr.getDeliveryTime(), false, new String[]{"validator.deliveryTime"}, null, "Required"));
            }

            i++;
        }
    }

    /**
     * Initializes the reproduction and its holdings.
     * Determines if we can already state the price and delivery time for one or more chosen holdings.
     *
     * @param reproduction The reproduction.
     */
    private void initReproduction(Reproduction reproduction, boolean isCreateInit) {
        // Already obtain the BTW percentage and the discount percentage, may we need it
        int btwPercentage = deliveryProperties.getReproductionBtwPercentage();
        int discountPercentage = reproduction.getDiscountPercentage();

        // Set the price and delivery time for each item
        int totalNoOfPages = 0;
        boolean chargeAdministrationCosts = false;
        for (HoldingReproduction hr : reproduction.getHoldingReproductions()) {
            ReproductionStandardOption standardOption = hr.getStandardOption();

            // Determine if we can specify the price, copyright price and delivery time, but have not done so yet
            if ((standardOption != null) && !hr.hasOrderDetails()) {
                hr.setPrice(standardOption.getPrice());

                // Determine the number of pages or copies, if possible
                Pages pages = hr.getHolding().getRecord().getPages();
                Copies copies = hr.getHolding().getRecord().getCopies();
                if (pages.containsNumberOfPages())
                    hr.setNumberOfPages(pages.getNumberOfPages());
                else if (copies.containsNumberOfCopies())
                    hr.setNumberOfPages(copies.getNumberOfCopies());
                else
                    hr.setNumberOfPages(1);

                hr.setDeliveryTime(standardOption.getDeliveryTime());
            }

            // If we have the order details, we can compute the discount and BTW
            if (hr.hasOrderDetails()) {
                // Compute the discount price
                hr.setDiscount(BigDecimalUtils.getPercentageOfAmount(hr.getCompletePrice(), discountPercentage));

                // Compute the BTW, only if the IISH owns the copyright
                if (hr.getHolding().getRecord().isCopyrightIISH()) {
                    hr.setBtwPercentage(btwPercentage);
                    hr.setBtwPrice(BigDecimalUtils.getBtwAmount(hr.getCompletePriceWithDiscount(), btwPercentage));
                }
                else {
                    hr.setBtwPercentage(0);
                    hr.setBtwPrice(BigDecimal.ZERO);
                }
            }

            // Check if we have to charge administration costs; if custom reproduction: always charge
            if (standardOption == null || standardOption.isAdministrationCosts())
                chargeAdministrationCosts = true;

            // Count the number of pages in this request, but only if there are only books in this request
            ExternalRecordInfo.MaterialType materialType
                    = hr.getHolding().getRecord().getExternalInfo().getMaterialType();
            if (standardOption != null && materialType == ExternalRecordInfo.MaterialType.BOOK)
                totalNoOfPages += hr.getNumberOfPages();
        }

        // Set the administration costs if initial create
        if (isCreateInit) {
            BigDecimal administrationCosts = new BigDecimal(deliveryProperties.getReproductionAdministrationCosts());
            if (!chargeAdministrationCosts &&
                    totalNoOfPages < deliveryProperties.getReproductionAdministrationCostsMinPages())
                administrationCosts = BigDecimal.ZERO;

            reproduction.setAdminstrationCosts(administrationCosts);
        }

        reproduction.setAdminstrationCostsDiscount(
                BigDecimalUtils.getPercentageOfAmount(reproduction.getAdminstrationCosts(), discountPercentage)
        );
        reproduction.setAdminstrationCostsBtwPercentage(btwPercentage);
        reproduction.setAdminstrationCostsBtwPrice(
                BigDecimalUtils.getBtwAmount(reproduction.getAdminstrationCostsWithDiscount(), btwPercentage));
    }

    /**
     * Validates and saves the standard reproduction options.
     *
     * @param standardOptions The standard reproduction options.
     * @param result          The binding result object to put the validation errors in.
     */
    public void editStandardOptions(ReproductionStandardOptions standardOptions, BindingResult result) {
        validateStandardOptions(standardOptions, result);

        // Add or save the records when no errors are present
        if (!result.hasErrors()) {
            addOrUpdateStandardOptions(standardOptions);
        }
    }

    /**
     * Validate reproduction standard options using the provided binding result to store errors.
     *
     * @param standardOptions The standard reproduction options.
     * @param result          The binding result.
     */
    private void validateStandardOptions(ReproductionStandardOptions standardOptions, BindingResult result) {
        int i = 0;
        for (ReproductionStandardOption standardOption : standardOptions.getOptions()) {
            result.pushNestedPath("options[" + i + "]");
            mvcValidator.validate(standardOption, result);
            result.popNestedPath();
            i++;
        }

        i = 0;
        for (ReproductionCustomNote customNote : standardOptions.getCustomNotes()) {
            result.pushNestedPath("customNotes[" + i + "]");
            mvcValidator.validate(customNote, result);
            result.popNestedPath();
            i++;
        }
    }

    /**
     * Returns the active reproduction with which this holding is associated.
     *
     * @param h The Holding to get the active reproduction of.
     * @return The active reproduction, or null if no active reproduction exists.
     */
    public Reproduction getActiveFor(Holding h) {
        return reproductionDAO.getActiveFor(h);
    }
}
