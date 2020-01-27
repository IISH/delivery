package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.User;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object of the user entities.
 */
public interface UserDAO {
    /**
     * Add a User to the database.
     *
     * @param obj User to add.
     */
    void add(User obj);

    /**
     * Remove a User from the database.
     *
     * @param obj User to remove.
     */
    void remove(User obj);

    /**
     * Save changes to a User in the database.
     *
     * @param obj User to save.
     */
    void save(User obj);

    /**
     * Retrieve the User matching the given Id.
     *
     * @param id Id of the User to retrieve.
     * @return The User matching the Id.
     */
    User getById(int id);

    /**
     * Get a criteria builder for querying Users.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Users matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Users.
     */
    List<User> list(CriteriaQuery<User> query);

    /**
     * Get a single User matching a built query.
     *
     * @param query The query to match by.
     * @return The matching User.
     */
    User get(CriteriaQuery<User> query);
}
