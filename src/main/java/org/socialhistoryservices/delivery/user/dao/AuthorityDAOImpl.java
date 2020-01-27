package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.Authority;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Represents the Data Access Object of the user permissions.
 */
@Repository
public class AuthorityDAOImpl implements AuthorityDAO {
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
     * Add a Authority to the database.
     *
     * @param obj Authority to add.
     */
    public void add(Authority obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Authority from the database.
     *
     * @param obj Authority to remove.
     */
    public void remove(Authority obj) {
        try {
            obj = entityManager.getReference(Authority.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
        entityManager.remove(obj);
    }

    /**
     * Save changes to a Authority in the database.
     *
     * @param obj Authority to save.
     */
    public void save(Authority obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Authority matching the given Id.
     *
     * @param id Id of the Authority to retrieve.
     * @return The Authority matching the Id.
     */
    public Authority getById(int id) {
        return entityManager.find(Authority.class, id);
    }

    /**
     * Get a criteria builder for querying Authorities.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Authorities matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching authorities.
     */
    public List<Authority> list(CriteriaQuery<Authority> query) {
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Get a single Authority matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Authority.
     */
    public Authority get(CriteriaQuery<Authority> query) {
        try {
            TypedQuery<Authority> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }
}
