package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Holding_;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction_;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction_;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.Date;
import java.util.List;

/**
 * Represents the Data Access object of a reproduction.
 */
@Repository
public class ReproductionDAOImpl implements ReproductionDAO {
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
     * Add a Reproduction to the database.
     *
     * @param obj Reproduction to add.
     */
    public synchronized void add(Reproduction obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Reproduction from the database.
     *
     * @param obj Reproduction to remove.
     */
    public void remove(Reproduction obj) {
        try {
            obj = entityManager.getReference(Reproduction.class, obj.getId());
            entityManager.remove(obj);
        } catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Reproduction in the database.
     *
     * @param obj Reproduction to save.
     */
    public void save(Reproduction obj) {
        // On save, cascading does not work for new holdings
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Reproduction matching the given Id.
     *
     * @param id Id of the Reproduction to retrieve.
     * @return The Reproduction matching the Id.
     */
    public Reproduction getById(int id) {
        return entityManager.find(Reproduction.class, id);
    }

    /**
     * Get a criteria builder for querying Reproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Reproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reproductions.
     */
    public List<Reproduction> list(CriteriaQuery<Reproduction> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    public List<Tuple> listForTuple(CriteriaQuery<Tuple> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a single Reproduction matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reproduction.
     */
    public Reproduction get(CriteriaQuery<Reproduction> query) {
        try {
            TypedQuery q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return (Reproduction) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Get an active reproduction relating to a specific Holding.
     *
     * @param h      Holding to find a reproduction for.
     * @return The active reproduction, null if none exist.
     */
    public Reproduction getActiveFor(Holding h) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reproduction> cq = cb.createQuery(Reproduction.class);
        Root<Reproduction> rRoot = cq.from(Reproduction.class);
        cq.select(rRoot);

        Join<Reproduction, HoldingReproduction> hrRoot = rRoot.join(Reproduction_.holdingReproductions);
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);
        Expression<Boolean> where = cb.equal(hRoot.get(Holding_.id), h.getId());
        where = cb.and(where, cb.equal(hrRoot.get(HoldingReproduction_.completed), false));

        cq.where(where);
        cq.orderBy(cb.asc(rRoot.get(Reproduction_.creationDate)));

        try {
            TypedQuery q = entityManager.createQuery(cq);
            q.setMaxResults(1);
            return (Reproduction) q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Check whether there are any reproductions made on the holding.
     *
     * @param h Holding to check for reproductions for.
     * @return Whether any reproductions have been made including this holding.
     */
    public boolean hasReproductions(Holding h) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reproduction> cq = cb.createQuery(Reproduction.class);
        Root<Reproduction> resRoot = cq.from(Reproduction.class);
        cq.select(resRoot);

        Join<Reproduction, HoldingReproduction> hrRoot = resRoot.join(Reproduction_.holdingReproductions);
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);
        Expression<Boolean> where = cb.equal(hRoot.get(Holding_.id), h.getId());
        cq.where(where);

        try {
            TypedQuery q = entityManager.createQuery(cq);
            q.setMaxResults(1);
            return q.getSingleResult() != null;
        } catch (NoResultException ex) {
            return false;
        }
    }
}
