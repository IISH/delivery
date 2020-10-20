package org.socialhistoryservices.delivery.permission.dao;

import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Represents the Data Access Object of Permissions (to request Records which have a restricted status).
 */
@Repository
public class PermissionDAOImpl implements PermissionDAO {
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
     * Add a Permission to the database.
     *
     * @param obj Permission to add.
     */
    public void add(Permission obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Permission from the database.
     *
     * @param obj Permission to remove.
     */
    public void remove(Permission obj) {
        try {
            obj = entityManager.getReference(Permission.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Permission in the database.
     *
     * @param obj Permission to save.
     */
    public void save(Permission obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Permission matching the given Id.
     *
     * @param id Id of the Permission to retrieve.
     * @return The Permission matching the Id.
     */
    public Permission getById(int id) {
        return entityManager.find(Permission.class, id);
    }

    /**
     * Get a criteria builder for querying Permissions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Permissions matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Permissions.
     */
    public List<Permission> list(CriteriaQuery<Permission> query) {
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * List all Permissions matching a built query.
     *
     * @param query       The query to match by.
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching Permissions.
     */
    public List<Permission> list(CriteriaQuery<Permission> query, int firstResult, int maxResults) {
        return entityManager
                .createQuery(query)
                .setFirstResult(firstResult)
                .setMaxResults(maxResults)
                .getResultList();
    }

    /**
     * Count all RecordPermissions matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q) {
        return entityManager.createQuery(q).getSingleResult();
    }

    /**
     * Get a single Permission matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Permission.
     */
    public Permission get(CriteriaQuery<Permission> query) {
        try {
            TypedQuery<Permission> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Check whether there are any permission requests made on the record.
     *
     * @param record Record to check for permission requests for.
     * @return Whether any permission requests have been made including this record.
     */
    public boolean hasPermissions(Record record) {
        String query = "select p from Permission p join p.record r where r.id = :id";

        Query q = entityManager.createQuery(query);
        q.setParameter("id", record.getId());
        q.setMaxResults(1);

        try {
            return q.getSingleResult() != null;
        }
        catch (NoResultException ex) {
            return false;
        }
    }
}
