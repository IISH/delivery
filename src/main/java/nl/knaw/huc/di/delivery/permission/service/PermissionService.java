package nl.knaw.huc.di.delivery.permission.service;

import nl.knaw.huc.di.delivery.permission.entity.Permission;
import nl.knaw.huc.di.delivery.record.entity.Record;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface to represent the service of the permission package.
 */
public interface PermissionService {
    /**
     * Add a Permission to the database.
     *
     * @param obj Permission to add.
     */
    void addPermission(Permission obj);

    /**
     * Remove a Permission from the database.
     *
     * @param obj Permission to remove.
     */
    void removePermission(Permission obj);

    /**
     * Save changes to a Permission in the database.
     *
     * @param obj Permission to save.
     */
    void savePermission(Permission obj);

    /**
     * Retrieve the Permission matching the given Id.
     *
     * @param id Id of the Permission to retrieve.
     * @return The Permission matching the Id.
     */
    @Deprecated()
    Permission getPermissionById(int id);

    Optional<Permission> findById(int id);

    /**
     * Get a criteria builder for querying Permissions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getPermissionCriteriaBuilder();

    /**
     * List all Permissions matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Permissions.
     */
    List<Permission> listPermissions(CriteriaQuery<Permission> query);

    /**
     * List all Permissions matching a built query.
     *
     * @param query       The query to match by.
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching Permissions.
     */
    List<Permission> listPermissions(CriteriaQuery<Permission> query, int firstResult, int maxResults);

    /**
     * Count all Permissions matching a built query.
     *
     * @param query The criteria query to execute
     * @return A count of matching Permissions.
     */
    long countPermissions(CriteriaQuery<Long> query);

    /**
     * Get a single Permission matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Permission.
     */
    Permission getPermission(CriteriaQuery<Permission> query);

    /**
     * Fetch a permission by its code.
     *
     * @param code The code of a permission.
     * @return The permission, or null if not found.
     */
    Permission getPermissionByCode(String code);

    /**
     * Check whether there are any permission requests made on the record.
     *
     * @param record Record to check for permission requests for.
     * @return Whether any permission requests have been made including this record.
     */
    boolean hasPermissions(Record record);

    Page<Permission> findAll(CriteriaQuery<Permission> cq, Pageable pageable);
}
