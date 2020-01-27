package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.Authority;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object of the user permissions.
 */
public interface AuthorityDAO {
    /**
     * Add a Authority to the database.
     *
     * @param obj Authority to add.
     */
    void add(Authority obj);

    /**
     * Remove a Authority from the database.
     *
     * @param obj Authority to remove.
     */
    void remove(Authority obj);

    /**
     * Save changes to a Authority in the database.
     *
     * @param obj Authority to save.
     */
    void save(Authority obj);

    /**
     * Retrieve the Authority matching the given Id.
     *
     * @param id Id of the Authority to retrieve.
     * @return The Authority matching the Id.
     */
    Authority getById(int id);

    /**
     * Get a criteria builder for querying Authorities.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Authorities matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching authorities.
     */
    List<Authority> list(CriteriaQuery<Authority> query);

    /**
     * Get a single Authority matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Authority.
     */
    Authority get(CriteriaQuery<Authority> query);
}
