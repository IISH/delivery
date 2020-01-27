package org.socialhistoryservices.delivery.permission.dao;

import org.socialhistoryservices.delivery.permission.entity.RecordPermission;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface to represent the Data Access Object of permissions on individual
 * records.
 */
@Repository
public class RecordPermissionDAOImpl implements RecordPermissionDAO {
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
     * Add a RecordPermission to the database.
     *
     * @param obj RecordPermission to add.
     */
    public void add(RecordPermission obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a RecordPermission from the database.
     *
     * @param obj RecordPermission to remove.
     */
    public void remove(RecordPermission obj) {
        try {
            obj = entityManager.getReference(RecordPermission.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a RecordPermission in the database.
     *
     * @param obj RecordPermission to save.
     */
    public void save(RecordPermission obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the RecordPermission matching the given Id.
     *
     * @param id Id of the RecordPermission to retrieve.
     * @return The RecordPermission matching the Id.
     */
    public RecordPermission getById(int id) {
        return entityManager.find(RecordPermission.class, id);
    }

    /**
     * Get a criteria builder for querying RecordPermissions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all RecordPermissions matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching RecordPermissions.
     */
    public List<RecordPermission> list(CriteriaQuery<RecordPermission> query) {
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Get a single RecordPermission matching a built query.
     *
     * @param query The query to match by.
     * @return The matching RecordPermission.
     */
    public RecordPermission get(CriteriaQuery<RecordPermission> query) {
        try {
            TypedQuery<RecordPermission> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }
}
