package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

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
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    long count(CriteriaQuery<Long> q);

    /**
     * Retrieve the HoldingReproduction matching the given ID.
     * @param id ID of the HoldingReproduction to retrieve.
     * @return The HoldingReproduction matching the ID.
     */
    HoldingReproduction getById(int id);
}
