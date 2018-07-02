package org.socialhistoryservices.delivery.record.dao;

import org.socialhistoryservices.delivery.record.entity.Record;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object of the Record.
 */
public interface RecordDAO {
    /**
     * Add a Record to the database.
     * @param obj Record to add.
     */
    void add(Record obj);

    /**
     * Remove a Record from the database.
     * @param obj Record to remove.
     */
    void remove(Record obj);

	/**
	 * Remove the ExternalRecordInfo of a Record from the database.
	 * @param obj Record of which to remove the ExternalRecordInfo.
	 */
    void removeExternalInfo(Record obj);

    /**
     * Save changes to a Record in the database.
     * @param obj Record to save.
     */
    void save(Record obj);

    /**
     * Retrieve the Record matching the given Id.
     * @param id Id of the Record to retrieve.
     * @return The Record matching the Id.
     */
    Record getById(int id);

    /**
     * Get a criteria builder for querying Records.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Records matching a built query.
     * @param query The query to match by.
     * @return A list of matching Records.
     */
    List<Record> list(CriteriaQuery<Record> query);

    /**
     * List all Records.
     * @param offset The offset.
     * @param maxResults The max number of records to fetch.
     * @return A list of Records.
     */
    List<Record> listIterable(int offset, int maxResults);

    /**
     * Get a single Record matching a built query.
     * @param query The query to match by.
     * @return The matching Record.
     */
    Record get(CriteriaQuery<Record> query);
}
