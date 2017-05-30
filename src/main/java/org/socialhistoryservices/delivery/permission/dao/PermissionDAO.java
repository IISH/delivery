package org.socialhistoryservices.delivery.permission.dao;

import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.permission.entity.RecordPermission;
import org.socialhistoryservices.delivery.record.entity.Record;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface to represent the Data Access Object of Permissions (to request
 * Records which have a restricted status).
 */
public interface PermissionDAO {
    /**
     * Add a Permission to the database.
     * @param obj Permission to add.
     */
    public void add(Permission obj);

    /**
     * Remove a Permission from the database.
     * @param obj Permission to remove.
     */
    public void remove(Permission obj);

    /**
     * Remove a RecordPermission from the database.
     * @param obj RecordPermission to remove.
     */
    public void remove(RecordPermission obj);

    /**
     * Save changes to a Permission in the database.
     * @param obj Permission to save.
     */
    public void save(Permission obj);

    /**
     * Retrieve the Permission matching the given Id.
     * @param id Id of the Permission to retrieve.
     * @return The Permission matching the Id.
     */
    public Permission getById(int id);

    /**
     * Get a criteria builder for querying Permissions.
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder();

    /**
     * List all RecordPermissions matching a built query.
     * @param query The query to match by.
     * @return A list of matching RecordPermissions.
     */
    public List<RecordPermission> list(CriteriaQuery<RecordPermission> query);

    /**
     * List all RecordPermissions matching a built query.
     * @param query The query to match by.
     * @param firstResult The first result to obtain
     * @param maxResults The max number of results to obtain
     * @return A list of matching RecordPermissions.
     */
    public List<RecordPermission> list(CriteriaQuery<RecordPermission> query, int firstResult, int maxResults);

    /**
     * Count all RecordPermissions matching a built query.
     * @param query The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> query);

    /**
     * Get a single Permission matching a built query.
     * @param query The query to match by.
     * @return The matching Permission.
     */
    public Permission get(CriteriaQuery<Permission> query);

    /**
     * Check whether there are any permission requests made on the record.
     * @param record Record to check for permission requests for.
     * @return Whether any permission requests have been made including this record.
     */
    public boolean hasPermissions(Record record);
}
