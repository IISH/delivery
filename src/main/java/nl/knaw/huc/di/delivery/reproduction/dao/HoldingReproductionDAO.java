package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing the Data Access object of a holding reproduction.
 */
public interface HoldingReproductionDAO {
    /**
     * Get a criteria builder for querying HoldingReproductions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReproductions.
     */
    List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q);

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReproductions.
     */
    List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q, int firstResult, int maxResults);

    /**
     * Count all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    long count(CriteriaQuery<Long> q);

    /**
     * Retrieve the HoldingReproduction matching the given ID.
     *
     * @param id ID of the HoldingReproduction to retrieve.
     * @return The HoldingReproduction matching the ID.
     */
    @Deprecated
    HoldingReproduction getById(int id);
    Optional<HoldingReproduction> findById(int id);

    Page<HoldingReproduction> findAll(CriteriaQuery<HoldingReproduction> cq, Pageable pageable);
}
