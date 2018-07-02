package org.socialhistoryservices.delivery.permission.dao;

import org.socialhistoryservices.delivery.permission.entity.RecordPermission;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface to represent the Data Access Object of permissions on individual
 * records.
 */
public interface RecordPermissionDAO {
    /**
     * Add a RecordPermission to the database.
     * @param obj RecordPermission to add.
     */
    void add(RecordPermission obj);

    /**
     * Remove a RecordPermission from the database.
     * @param obj RecordPermission to remove.
     */
    void remove(RecordPermission obj);

    /**
     * Save changes to a RecordPermission in the database.
     * @param obj RecordPermission to save.
     */
    void save(RecordPermission obj);

    /**
     * Retrieve the RecordPermission matching the given Id.
     * @param id Id of the RecordPermission to retrieve.
     * @return The RecordPermission matching the Id.
     */
    RecordPermission getById(int id);

    /**
     * Get a criteria builder for querying RecordPermissions.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all RecordPermissions matching a built query.
     * @param query The query to match by.
     * @return A list of matching RecordPermissions.
     */
    List<RecordPermission> list(CriteriaQuery<RecordPermission> query);

    /**
     * Get a single RecordPermission matching a built query.
     * @param query The query to match by.
     * @return The matching RecordPermission.
     */
    RecordPermission get(CriteriaQuery<RecordPermission> query);
}
