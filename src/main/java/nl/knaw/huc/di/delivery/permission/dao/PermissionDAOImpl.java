package nl.knaw.huc.di.delivery.permission.dao;

import nl.knaw.huc.di.delivery.permission.entity.Permission;
import nl.knaw.huc.di.delivery.record.entity.Record;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access Object of Permissions (to request Records which have a restricted status).
 */
@Service
public class PermissionDAOImpl implements PermissionDAO {

    private final PermissionRepository permissionRepository;

    public PermissionDAOImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Add a Permission to the database.
     *
     * @param obj Permission to add.
     */
    public void add(Permission obj) {
        permissionRepository.save(obj);
    }

    /**
     * Remove a Permission from the database.
     *
     * @param obj Permission to remove.
     */
    public void remove(Permission obj) {
        permissionRepository.delete(obj);
    }

    /**
     * Save changes to a Permission in the database.
     *
     * @param obj Permission to save.
     */
    public void save(Permission obj) {
        permissionRepository.save(obj);
    }

    /**
     * Retrieve the Permission matching the given Id.
     *
     * @param id Id of the Permission to retrieve.
     * @return The Permission matching the Id.
     */
    @Deprecated
    public Permission getById(int id) {
        return findById(id).orElse(null);
    }

    /**
     * Retrieve the Permission matching the given Id.
     *
     * @param id Id of the Permission to retrieve.
     * @return The Permission matching the Id.
     */
    public Optional<Permission> findById(int id) {
        return permissionRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Permissions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return permissionRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Permissions matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Permissions.
     */
    public List<Permission> list(CriteriaQuery<Permission> query) {
        return permissionRepository.findAll(query);
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
        return new ArrayList<>(0); // todo gebruik pager
    }

    /**
     * Count all RecordPermissions matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q) {
        return 0;// todo gebruik pager
    }

    /**
     * Get a single Permission matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Permission.
     */
    public Permission get(CriteriaQuery<Permission> query) {
        List<Permission> byCriteriaQuery = permissionRepository.findAll(query);
        return (byCriteriaQuery.isEmpty()) ? null : byCriteriaQuery.getFirst();
    }

    /**
     * Check whether there are any permission requests made on the record.
     *
     * @param record Record to check for permission requests for.
     * @return Whether any permission requests have been made including this record.
     */
    public boolean hasPermissions(Record record) {
        return permissionRepository.existsPermissionByRecord(record);
    }

    public Page<Permission> findAll(CriteriaQuery<Permission> query, Pageable pageable) {
        return permissionRepository.findAll(query, pageable);
    }
}
