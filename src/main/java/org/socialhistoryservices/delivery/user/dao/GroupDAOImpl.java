package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.Group;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Represents the Data Access Object of the user groups.
 */
@Repository
public class GroupDAOImpl implements GroupDAO {
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
     * Add a Group to the database.
     *
     * @param obj Group to add.
     */
    public void add(Group obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Group from the database.
     *
     * @param obj Group to remove.
     */
    public void remove(Group obj) {
        try {
            obj = entityManager.getReference(Group.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Group in the database.
     *
     * @param obj Group to save.
     */
    public void save(Group obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Group matching the given Id.
     *
     * @param id Id of the Group to retrieve.
     * @return The Group matching the Id.
     */
    public Group getById(int id) {
        return entityManager.find(Group.class, id);
    }

    /**
     * Get a criteria builder for querying Groups.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Groups matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Groups.
     */
    public List<Group> list(CriteriaQuery<Group> query) {
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Get a single Group matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Group.
     */
    public Group get(CriteriaQuery<Group> query) {
        try {
            TypedQuery<Group> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }
}
