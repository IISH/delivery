package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption;
import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption_;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Represents the Data Access object of a reproduction standard option.
 */
@Repository
public class ReproductionStandardOptionDAOImpl implements ReproductionStandardOptionDAO {
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
     * Add a ReproductionStandardOption to the database.
     *
     * @param obj ReproductionStandardOption to add.
     */
    public void add(ReproductionStandardOption obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a ReproductionStandardOption from the database.
     *
     * @param obj ReproductionStandardOption to remove.
     */
    public void remove(ReproductionStandardOption obj) {
        try {
            obj = entityManager.getReference(ReproductionStandardOption.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a ReproductionStandardOption in the database.
     *
     * @param obj ReproductionStandardOption to save.
     */
    public void save(ReproductionStandardOption obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the ReproductionStandardOption matching the given Id.
     *
     * @param id Id of the ReproductionStandardOption to retrieve.
     * @return The ReproductionStandardOption matching the Id.
     */
    public ReproductionStandardOption getById(int id) {
        return entityManager.find(ReproductionStandardOption.class, id);
    }

    /**
     * Get a criteria builder for querying ReproductionStandardOptions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all ReproductionStandardOptions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching ReproductionStandardOptions.
     */
    public List<ReproductionStandardOption> list(CriteriaQuery<ReproductionStandardOption> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a single ReproductionStandardOption matching a built query.
     *
     * @param query The query to match by.
     * @return The matching ReproductionStandardOption.
     */
    public ReproductionStandardOption get(CriteriaQuery<ReproductionStandardOption> query) {
        try {
            TypedQuery<ReproductionStandardOption> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * List all ReproductionStandardOptions.
     *
     * @return A list of ReproductionStandardOptions.
     */
    public List<ReproductionStandardOption> listAll() {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<ReproductionStandardOption> cq = cb.createQuery(ReproductionStandardOption.class);
        Root<ReproductionStandardOption> root = cq.from(ReproductionStandardOption.class);

        cq.select(root);
        cq.orderBy(
                cb.asc(root.get(ReproductionStandardOption_.materialType)),
                cb.asc(root.get(ReproductionStandardOption_.price)),
                cb.asc(root.get(ReproductionStandardOption_.deliveryTime)));

        return list(cq);
    }
}
