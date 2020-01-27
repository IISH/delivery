package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Represents the Data Access object of a holding reproduction.
 */
@Repository
public class HoldingReproductionDAOImpl implements HoldingReproductionDAO {
    private EntityManager entityManager;

    /**
     * Set the entity manager to use in this DAO, internal.
     *
     * @param entityManager The manager.
     */
    @PersistenceContext
    private void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get a criteria builder for querying HoldingReproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q, int firstResult, int maxResults) {
        return entityManager
                .createQuery(q)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    /**
     * Count all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q) {
        return entityManager.createQuery(q).getSingleResult();
    }

    /**
     * Retrieve the HoldingReproduction matching the given ID.
     *
     * @param id ID of the HoldingReproduction to retrieve.
     * @return The HoldingReproduction matching the ID.
     */
    public HoldingReproduction getById(int id) {
        return entityManager.find(HoldingReproduction.class, id);
    }
}
