package org.socialhistoryservices.delivery.record.service;

import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.record.entity.Record;
import org.springframework.validation.BindingResult;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the service of the Record package.
 */
public interface RecordService {
    /**
     * Add a Record to the database.
     * @param obj Record to add.
     */
    void addRecord(Record obj);

    /**
     * Remove a Record from the database.
     * @param obj Record to remove.
     */
    void removeRecord(Record obj);

    /**
     * Save changes to a Record in the database.
     * @param obj Record to save.
     */
    void saveRecord(Record obj);

    /**
     * Save changes to a Holding in the database.
     * @param obj Holding to save.
     */
    void saveHolding(Holding obj);

    /**
     * Retrieve the Record matching the given Id.
     *
     * @param id Id of the Record to retrieve.
     * @return The Record matching the Id.
     */
    Record getRecordById(int id);

    /**
     * Retrieve the Record matching the given pid.
     * @param pid Pid of the Record to retrieve.
     * @return The Record matching the pid. Null if none exist.
     */
    Record getRecordByPid(String pid);

    /**
     * Get a criteria builder for querying Records.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getRecordCriteriaBuilder();

    /**
     * List all Records matching a built query.
     * @param query The query to match by.
     * @return A list of matching Records.
     */
    List<Record> listRecords(CriteriaQuery<Record> query);

    /**
     * List all Records.
     * @param offset     The offset.
     * @param maxResults The max number of records to fetch.
     * @return A list of Records.
     */
    List<Record> listIterable(int offset, int maxResults);

    /**
     * Get a single Record matching a built query.
     * @param query The query to match by.
     * @return The matching Record.
     */
    Record getRecord(CriteriaQuery<Record> query);

    /**
     * Retrieve the Holding matching the given Id.
     * @param id Id of the Holding to retrieve.
     * @return The Holding matching the Id.
     */
    Holding getHoldingById(int id);

    /**
     * List all Holdings matching a built query.
     * @param query The query to match by.
     * @return A list of matching Holdings.
     */
    List<Holding> listHoldings(CriteriaQuery<Holding> query);


    /**
     * Remove a Holding from the database.
     * @param obj Holding to remove.
     */
    void removeHolding(Holding obj);

    /**
     * Updates the status of a holding.
     * @param holding The holding.
     * @param status  The new status.
     */
    void updateHoldingStatus(Holding holding, Holding.Status status);

    /**
     * Edit records.
     * @param newRecord The new record to put.
     * @param oldRecord The old record (or null if none).
     * @param result    The binding result object to put the validation errors in.
     * @throws NoSuchParentException Thrown when the provided record is detected as a pid by containing an
     *                               item separator (default .), but the parent record was not found in the
     *                               database.
     */
    void createOrEdit(Record newRecord, Record oldRecord, BindingResult result) throws NoSuchParentException;

    /**
     * Create a record, using the metadata from the IISH API to populate its
     * fields.
     * @param pid The pid of the record (should exist in the API).
     * @return The new Record (not yet committed to the database).
     * @throws NoSuchPidException Thrown when the provided PID does not exist in the API.
     */
    Record createRecordByPid(String pid) throws NoSuchPidException;

    /**
     * Updates the external info of the given record, if necessary.
     * @param record      The record of which to update the external info.
     * @param hardRefresh Always update the external info.
     * @return Whether the record was updated.
     */
    boolean updateExternalInfo(Record record, boolean hardRefresh);

    /**
     * Get all child records of the given record that are currently reserved.
     * @param record The parent record.
     * @return A list of all reserved child records.
     */
    List<Record> getReservedChildRecords(Record record);

    /*
     * Get all sibling records with the same container.
     * @param record The record.
     * @return A list of all sibling records with the same container.
     */
    List<Record> getSiblingsWithSameContainer(Record record);
}
