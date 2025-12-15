package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.Order;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Represents the Data Access object of a order.
 */
@Repository
public class OrderDAOImpl implements OrderDAO {
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
     * Add a Order to the database.
     *
     * @param obj Order to add.
     */
    public void add(Order obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Order from the database.
     *
     * @param obj Order to remove.
     */
    public void remove(Order obj) {
        try {
            obj = entityManager.getReference(Order.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Order in the database.
     *
     * @param obj Order to save.
     */
    public void save(Order obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Order matching the given Id.
     *
     * @param id Id of the Order to retrieve.
     * @return The Order matching the Id.
     */
    public Order getById(String id) {
        return entityManager.find(Order.class, id);
    }

    /**
     * Get a criteria builder for querying Orders.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Orders matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Orders.
     */
    public List<Order> list(CriteriaQuery<Order> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a single Order matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Order.
     */
    public Order get(CriteriaQuery<Order> query) {
        try {
            TypedQuery<Order> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }
}
