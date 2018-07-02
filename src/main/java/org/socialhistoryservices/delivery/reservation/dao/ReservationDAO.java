package org.socialhistoryservices.delivery.reservation.dao;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access bject of a reservation.
 */
public interface ReservationDAO {
    /**
     * Add a Reservation to the database.
     * @param obj Reservation to add.
     */
    void add(Reservation obj);

    /**
     * Remove a Reservation from the database.
     * @param obj Reservation to remove.
     */
    void remove(Reservation obj);

    /**
     * Save changes to a Reservation in the database.
     * @param obj Reservation to save.
     */
    void save(Reservation obj);

    /**
     * Retrieve the Reservation matching the given Id.
     * @param id Id of the Reservation to retrieve.
     * @return The Reservation matching the Id.
     */
    Reservation getById(int id);

    /**
     * Get a criteria builder for querying Reservations.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Reservations matching a built query.
     * @param q The criteria query to execute
     * @return A list of matching Reservations.
     */
    List<Reservation> list(CriteriaQuery<Reservation> q);

	/**
	 * List all Tuples matching a built query.
	 * @param q The criteria query to execute
	 * @return A list of matching Tuples.
	 */
    List<Tuple> listForTuple(CriteriaQuery<Tuple> q);

    /**
     * Get a single Reservation matching a built query.
     * @param query The query to match by.
     * @return The matching Reservation.
     */
    Reservation get(CriteriaQuery<Reservation> query);

    /**
     * Get an active reservation relating to a specific Holding.
     * @param h Holding to find a reservation for.
     * @return The active reservation, null if none exist.
     */
    Reservation getActiveFor(Holding h);

    /**
     * Check whether there are any reservations made on the holding.
     * @param h Holding to check for reservations for.
     * @return Whether any reservations have been made including this holding.
     */
    boolean hasReservations(Holding h);
}
