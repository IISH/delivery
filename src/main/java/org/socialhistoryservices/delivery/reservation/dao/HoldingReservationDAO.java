package org.socialhistoryservices.delivery.reservation.dao;

import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access bject of a holding reservation.
 */
public interface HoldingReservationDAO {
    /**
     * Get a criteria builder for querying HoldingReservations.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReservations.
     */
    List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q);

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReservations.
     */
    List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q, int firstResult, int maxResults);

    /**
     * Count all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    long count(CriteriaQuery<Long> q);

    /**
     * Retrieve the HoldingReservation matching the given ID.
     *
     * @param id ID of the HoldingReservation to retrieve.
     * @return The HoldingReservation matching the ID.
     */
    HoldingReservation getById(int id);
}
