package org.socialhistoryservices.delivery.reservation.dao;

import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

public interface ReservationDateExceptionDAO {
    /**
     * Add a ReservationDateExceptions to the database.
     *
     * @param obj Reservation to add.
     */
    void add(ReservationDateException obj);

    /**
     * Remove a ReservationDateExceptions from the database.
     *
     * @param obj Reservation to remove.
     */
    void remove(ReservationDateException obj);

    /**
     * Save changes to a ReservationDateExceptions in the database.
     *
     * @param obj Reservation to save.
     */
    void save(ReservationDateException obj);

    /**
     * List all ReservationDateExceptions matching a built query.
     *
     * @param q The criteria query to execute.
     * @return A list of matching ReservationDateExceptions.
     */
    List<ReservationDateException> list(CriteriaQuery<ReservationDateException> q);

    /**
     * Get a criteria builder for querying ReservationDateExceptions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * Get a ReservationDateException matching a given id.
     *
     * @param id The id to match the ReservationDateException on.
     * @return A ReservationDateException matching the id.
     */
    ReservationDateException getById(int id);
}
