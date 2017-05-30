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
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder();

    /**
     * List all HoldingReservations matching a built query.
     * @param q The criteria query to execute
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q);

    /**
     * List all HoldingReservations matching a built query.
     * @param q The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults The max number of results to obtain
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q, int firstResult, int maxResults);

    /**
     * Count all HoldingReservations matching a built query.
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q);
}
