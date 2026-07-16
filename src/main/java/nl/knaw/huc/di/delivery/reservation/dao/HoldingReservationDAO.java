package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

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
    @Deprecated
    HoldingReservation getById(int id);
    Optional<HoldingReservation> findById(int id);

    Page<HoldingReservation> findAll(CriteriaQuery<HoldingReservation> cq, Pageable pageable);
}
