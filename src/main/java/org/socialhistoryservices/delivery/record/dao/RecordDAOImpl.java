package org.socialhistoryservices.delivery.record.dao;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.socialhistoryservices.delivery.record.entity.Record_;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Represents the Data Access Object of a Record.
 */
@Repository
public class RecordDAOImpl implements RecordDAO {
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
     * Add a Record to the database.
     *
     * @param obj Record to add.
     */
    public void add(Record obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Record from the database.
     *
     * @param obj Record to remove.
     */
    public void remove(Record obj) {
        try {
            obj = entityManager.getReference(Record.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Remove the ExternalRecordInfo of a Record from the database.
     *
     * @param obj Record of which to remove the ExternalRecordInfo.
     */
    public void removeExternalInfo(Record obj) {
        try {
            ExternalRecordInfo eriObj = obj.getExternalInfo();
            eriObj = entityManager.getReference(ExternalRecordInfo.class, eriObj.getId());
            entityManager.remove(eriObj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Record in the database.
     *
     * @param obj Record to save.
     */
    public void save(Record obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the Record matching the given Id.
     *
     * @param id Id of the Record to retrieve.
     * @return The Record matching the Id.
     */
    public Record getById(int id) {
        return entityManager.find(Record.class, id);
    }

    /**
     * Get a criteria builder for querying Records.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Records matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Records.
     */
    public List<Record> list(CriteriaQuery<Record> query) {
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * List all Records.
     *
     * @param offset     The offset.
     * @param maxResults The max number of records to fetch.
     * @return A list of Records.
     */
    public List<Record> listIterable(int offset, int maxResults) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Record> query = cb.createQuery(Record.class);

        Root<Record> rRoot = query.from(Record.class);
        query.select(rRoot);
        query.orderBy(cb.asc(rRoot.get(Record_.id)));

        return entityManager
                .createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(maxResults)
                .getResultList();
    }

    /**
     * Get a single Record matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Record.
     */
    public Record get(CriteriaQuery<Record> query) {
        try {
            TypedQuery<Record> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }
}
