package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.request.service.ClosedException;
import org.socialhistoryservices.delivery.request.service.NoHoldingsException;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.springframework.validation.BindingResult;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.awt.print.PrinterException;
import java.util.Date;
import java.util.List;

/**
 * Interface representing the service of the reservation package.
 */
public interface ReservationService {
    /**
     * Add a Reservation to the database.
     *
     * @param obj Reservation to add.
     */
    void addReservation(Reservation obj);

    /**
     * Remove a Reservation from the database.
     *
     * @param obj Reservation to remove.
     */
    void removeReservation(Reservation obj);

    /**
     * Save changes to a Reservation in the database.
     *
     * @param obj Reservation to save.
     */
    Reservation saveReservation(Reservation obj);

    /**
     * Retrieve the Reservation matching the given Id.
     *
     * @param id Id of the Reservation to retrieve.
     * @return The Reservation matching the Id.
     */
    Reservation getReservationById(int id);

    /**
     * Get a criteria builder for querying Reservations.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getReservationCriteriaBuilder();

    /**
     * Get a criteria builder for querying HoldingReservations.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getHoldingReservationCriteriaBuilder();

    /**
     * List all Reservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reservations.
     */
    List<Reservation> listReservations(CriteriaQuery<Reservation> q);

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    List<Tuple> listTuples(CriteriaQuery<Tuple> q);

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReservations.
     */
    List<HoldingReservation> listHoldingReservations(CriteriaQuery<HoldingReservation> q);

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReservations.
     */
    List<HoldingReservation> listHoldingReservations(CriteriaQuery<HoldingReservation> q,
                                                     int firstResult, int maxResults);

    /**
     * Count all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A count of matching HoldingReservations.
     */
    long countHoldingReservations(CriteriaQuery<Long> q);

    /**
     * Get a single Reservation matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reservation.
     */
    Reservation getReservation(CriteriaQuery<Reservation> query);

    /**
     * Change the status of all holdings in a reservation.
     *
     * @param res    Reservation to change status for.
     * @param status Status to change holdings to.
     */
    void changeHoldingStatus(Reservation res, Holding.Status status);

    /**
     * Mark a specific item in a reservation as seen, bumping it to the next status.
     *
     * @param res Reservation to change status for.
     * @param h   Holding to bump.
     */
    void markItem(Reservation res, Holding h);

    /**
     * Merge the other reservation's fields into this reservation.
     *
     * @param reservation The reservation.
     * @param other       The other reservation to merge with.
     */
    void merge(Reservation reservation, Reservation other);

    /**
     * Set the reservation status and update the associated holdings status
     * accordingly. Only updates status forward.
     *
     * @param reservation The reservation.
     * @param status      The reservation which changed status.
     */
    void updateStatusAndAssociatedHoldingStatus(Reservation reservation, Reservation.Status status);

    /**
     * Print a reservation if it was not printed yet.
     *
     * @param res The reservation to print.
     * @throws PrinterException Thrown when delivering the print job to the
     *                          printer failed. Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    void printReservation(Reservation res) throws PrinterException;

    /**
     * Prints holding reservations by using the default printer.
     *
     * @param hrs         The holding reservations to print.
     * @param alwaysPrint If set to true, already printed holdings will
     *                    also be printed.
     * @throws PrinterException Thrown when delivering the print job to the
     *                          printer failed. Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    void printItems(List<HoldingReservation> hrs, boolean alwaysPrint) throws PrinterException;

    /**
     * Edit reservations.
     *
     * @param newRes The new reservation to put in the database.
     * @param oldRes The old reservation in the database (if present).
     * @param result The binding result object to put the validation errors in.
     * @throws ClosedException     Thrown when a holding is provided which
     *                             references a record which is restrictionType=CLOSED.
     * @throws NoHoldingsException Thrown when no holdings are provided.
     */
    void createOrEdit(Reservation newRes, Reservation oldRes, BindingResult result)
            throws ClosedException, NoHoldingsException;

    /**
     * Validate provided holding part of request.
     *
     * @param newReq The new request containing holdings.
     * @param oldReq The old request if applicable (or null).
     * @throws ClosedException     Thrown when a holding is provided which
     *                             references a record which is restrictionType=CLOSED.
     * @throws NoHoldingsException Thrown when no holdings are provided.
     */
    void validateHoldings(Request newReq, Request oldReq) throws NoHoldingsException, ClosedException;

    /**
     * Get the first valid reservation date after or equal to from.
     *
     * @param from The date to start from.
     * @return The first valid date, or null when maxDaysInAdvance was exceeded.
     */
    Date getFirstValidReservationDate(Date from);

    /**
     * Returns the active reservation with which this holding is associated.
     *
     * @param holding The Holding to get the active reservation of.
     * @return The active reservation, or null if no active reservation exists.
     */
    Reservation getActiveFor(Holding holding);
}
