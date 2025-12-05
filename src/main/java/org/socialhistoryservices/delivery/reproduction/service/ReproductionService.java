package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.reproduction.entity.*;
import org.socialhistoryservices.delivery.reproduction.util.ReproductionStandardOptions;
import org.socialhistoryservices.delivery.request.service.ClosedException;
import org.socialhistoryservices.delivery.request.service.NoHoldingsException;
import org.springframework.validation.BindingResult;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.awt.print.PrinterException;
import java.util.List;
import java.util.Map;

/**
 * Interface representing the service of the Reproduction package.
 */
public interface ReproductionService {
    /**
     * Remove a Reproduction from the database.
     *
     * @param reproduction Reproduction to remove.
     */
    void removeReproduction(Reproduction reproduction);

    /**
     * Change the status of all holdings in a reproduction.
     *
     * @param reproduction Reproduction to change status for.
     * @param status       Status to change holdings to.
     */
    void changeHoldingStatus(Reproduction reproduction, Holding.Status status);

    /**
     * Retrieve the Reproduction matching the given id.
     *
     * @param id Id of the Reproduction to retrieve.
     * @return The Reproduction matching the id.
     */
    Reproduction getReproductionById(int id);

    /**
     * Retrieve the Order matching the given id.
     *
     * @param id Id of the Order to retrieve.
     * @return The Order matching the id.
     */
    Order getOrderById(String id);

    /**
     * Retrieve the ReproductionStandardOption matching the given id.
     *
     * @param id Id of the ReproductionStandardOption to retrieve.
     * @return The ReproductionStandardOption matching the id.
     */
    ReproductionStandardOption getReproductionStandardOptionById(int id);

    /**
     * Get the first Reproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return The first matching Reproduction.
     */
    Reproduction getReproduction(CriteriaQuery<Reproduction> q);

    /**
     * List all Reproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reproductions.
     */
    List<Reproduction> listReproductions(CriteriaQuery<Reproduction> q);

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    List<Tuple> listTuples(CriteriaQuery<Tuple> q);

    /**
     * List all HoldingReproduction matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReproductions.
     */
    List<HoldingReproduction> listHoldingReproductions(CriteriaQuery<HoldingReproduction> q);

    /**
     * List all HoldingReproduction matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReproductions.
     */
    List<HoldingReproduction> listHoldingReproductions(CriteriaQuery<HoldingReproduction> q,
                                                       int firstResult, int maxResults);

    /**
     * Count all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A count of matching HoldingReproductions.
     */
    long countHoldingReproductions(CriteriaQuery<Long> q);

    /**
     * Returns all standard options for reproductions.
     *
     * @return A list with all standard options for reproductions.
     */
    List<ReproductionStandardOption> getAllReproductionStandardOptions();

    /**
     * Returns all custom notes for reproductions.
     *
     * @return A list with all custom notes for reproductions.
     */
    List<ReproductionCustomNote> getAllReproductionCustomNotes();

    /**
     * Returns all custom notes for reproductions.
     *
     * @return A map with all custom notes for reproductions by material type name.
     */
    Map<String, ReproductionCustomNote> getAllReproductionCustomNotesAsMap();

    /**
     * Get a criteria builder for querying Reproductions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getReproductionCriteriaBuilder();

    /**
     * Get a criteria builder for querying HoldingReproductions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getHoldingReproductionCriteriaBuilder();

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
    void createOrEdit(Reproduction newReproduction, Reproduction oldReproduction, BindingResult result,
                      boolean isCustomer)
            throws ClosedException, NoHoldingsException, ClosedForReproductionException;

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
    void validateReproductionHoldings(Reproduction newRep, Reproduction oldRep)
            throws NoHoldingsException, ClosedException, ClosedForReproductionException;

    /**
     * Whether the price and delivery time is determined for all holdings and
     * as a result the reproduction has all the order details.
     *
     * @param reproduction The reproduction.
     * @return Whether all holdings have order details.
     */
    boolean hasOrderDetails(Reproduction reproduction);

    /**
     * Returns standard options for the given holding which are NOT available in the SOR.
     *
     * @param holding                     The holding.
     * @param reproductionStandardOptions The standard options to choose from.
     * @return A list of options that are currently not available online in the SOR.
     */
    List<ReproductionStandardOption> getStandardOptionsNotInSor(Holding holding,
                                                                List<ReproductionStandardOption> reproductionStandardOptions);

    /**
     * Returns standard options for the given holding which ARE available in the SOR,
     * and therefor does not accept the standard options.
     *
     * @param holding                     The holding.
     * @param reproductionStandardOptions The standard options to choose from.
     * @return A list of options that are currently not available online in the SOR.
     */
    List<ReproductionStandardOption> getStandardOptionsInSorOnlyCustom(Holding holding,
                                                                       List<ReproductionStandardOption> reproductionStandardOptions);

    /**
     * Scheduled task to cancel all reproductions not payed within 5 days after the offer was ready.
     */
    void checkPayedReproductions();

    /**
     * Scheduled task to send a reminder for all reproductions not paid within the time frame after the offer was ready.
     */
    void checkReminderReproductions();

    /**
     * Creates an order for the given reproduction.
     *
     * @param r The reproduction.
     * @return The created and registered order. (Or null if the reproduction is for free)
     * @throws IncompleteOrderDetailsException   Thrown when not all holdings have an order ready.
     * @throws OrderRegistrationFailureException Thrown in case we failed to register the order in Mollie.
     */
    Order createOrder(Reproduction r) throws IncompleteOrderDetailsException, OrderRegistrationFailureException;

    /**
     * Will refresh the given order by retrieving the payment details from Mollie.
     *
     * @param order The order to refresh. The id must be set.
     * @return The refreshed Order when successful.
     */
    Order refreshOrder(Order order);

    /**
     * Returns whether the record accepts the standard reproduction option.
     *
     * @param record         The record to check.
     * @param standardOption The standard reproduction option.
     * @return Whether the record accepts the given standard reproduction option.
     */
    boolean recordAcceptsReproductionOption(Record record, ReproductionStandardOption standardOption);

    /**
     * Validates and saves the standard reproduction options.
     *
     * @param standardOptions The standard reproduction options.
     * @param result          The binding result object to put the validation errors in.
     */
    void editStandardOptions(ReproductionStandardOptions standardOptions, BindingResult result);

    /**
     * Add a Reproduction to the database.
     *
     * @param obj Reproduction to add.
     */
    void addReproduction(Reproduction obj);

    /**
     * Save changes to a Reproduction in the database.
     *
     * @param obj Reproduction to save.
     */
    Reproduction saveReproduction(Reproduction obj);

    /**
     * Prints holding reproductions by using the default printer.
     *
     * @param hrs         The holding reproductions to print.
     * @param alwaysPrint If set to true, already printed reproductions will also be printed.
     * @throws PrinterException Thrown when delivering the print job to the printer failed.
     *                          Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    void printItems(List<HoldingReproduction> hrs, boolean alwaysPrint) throws PrinterException;

    /**
     * Print a reproduction if it was not printed yet.
     *
     * @param reproduction The reproduction to print.
     * @throws PrinterException Thrown when delivering the print job to the printer failed.
     *                          Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    void printReproduction(Reproduction reproduction) throws PrinterException;

    /**
     * Mark a specific item in a reproduction as seen, bumping it to the next status.
     *
     * @param r Reproduction to change status for.
     * @param h Holding to bump.
     */
    void markItem(Reproduction r, Holding h);

    /**
     * Merge the other reproduction's fields into this reproduction.
     *
     * @param reproduction The reproduction.
     * @param other        The other reproduction.
     */
    void merge(Reproduction reproduction, Reproduction other);

    /**
     * Set the reproduction status and update the associated holdings status accordingly.
     * Only updates status forward.
     *
     * @param reproduction The reproduction.
     * @param status       The reproduction which changed status.
     */
    void updateStatusAndAssociatedHoldingStatus(Reproduction reproduction, Reproduction.Status status);

    /**
     * Auto print all holdings of the given reproduction, if possible.
     * Run this in a separate thread, we do nothing on failure so in this case this is perfectly possible.
     *
     * @param reproduction The reproduction.
     */
    void autoPrintReproduction(final Reproduction reproduction);

    /**
     * Returns the active reproduction with which this holding is associated.
     *
     * @param h The Holding to get the active reproduction of.
     * @return The active reproduction, or null if no active reproduction exists.
     */
    Reproduction getActiveFor(Holding h);
}
