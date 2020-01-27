package org.socialhistoryservices.delivery.user.dao;

import org.socialhistoryservices.delivery.user.entity.Group;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object for user groups.
 */
public interface GroupDAO {
    /**
     * Add a Group to the database.
     *
     * @param obj Group to add.
     */
    void add(Group obj);

    /**
     * Remove a Group from the database.
     *
     * @param obj Group to remove.
     */
    void remove(Group obj);

    /**
     * Save changes to a Group in the database.
     *
     * @param obj Group to save.
     */
    void save(Group obj);

    /**
     * Retrieve the Group matching the given Id.
     *
     * @param id Id of the Group to retrieve.
     * @return The Group matching the Id.
     */
    Group getById(int id);

    /**
     * Get a criteria builder for querying Groups.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Groups matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Groups.
     */
    List<Group> list(CriteriaQuery<Group> query);

    /**
     * Get a single Group matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Group.
     */
    Group get(CriteriaQuery<Group> query);
}
