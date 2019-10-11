package org.socialhistoryservices.delivery.record.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.socialhistoryservices.delivery.api.NoSuchPidException;
import org.socialhistoryservices.delivery.api.RecordLookupService;
import org.socialhistoryservices.delivery.config.DeliveryProperties;
import org.socialhistoryservices.delivery.record.dao.HoldingDAO;
import org.socialhistoryservices.delivery.record.dao.RecordDAO;
import org.socialhistoryservices.delivery.record.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import javax.persistence.criteria.*;
import java.util.*;

/**
 * Represents the service of the record package.
 */
@Service
@Transactional
public class RecordServiceImpl implements RecordService {

    @Autowired
    private RecordDAO recordDAO;

    @Autowired
    private HoldingDAO holdingDAO;

    @Autowired
    private DeliveryProperties deliveryProperties;

    @Autowired
    private Validator mvcValidator;

    @Autowired
    private RecordLookupService lookup;

    private static final Log LOGGER = LogFactory.getLog(RecordServiceImpl.class);

    /**
     * Add a Record to the database.
     * @param obj Record to add.
     */
    public void addRecord(Record obj) {
        recordDAO.add(obj);
    }

    /**
     * Remove a Record from the database.
     * @param obj Record to remove.
     */
    public void removeRecord(Record obj) {
        recordDAO.remove(obj);
    }

    /**
     * Save changes to a Record in the database.
     * @param obj Record to save.
     */
    public void saveRecord(Record obj) {
        recordDAO.save(obj);
    }

    /**
     * Save changes to a Holding in the database.
     * @param obj Holding to save.
     */
    public void saveHolding(Holding obj) {
        holdingDAO.save(obj);
    }

    /**
     * Retrieve the Record matching the given Id.
     * @param id Id of the Record to retrieve.
     * @return The Record matching the Id.
     */
    public Record getRecordById(int id) {
        return recordDAO.getById(id);
    }

    /**
     * Retrieve the Record matching the given pid.
     * @param pid Pid of the Record to retrieve.
     * @return The Record matching the pid. Null if none exist.
     */
    public Record getRecordByPid(String pid) {
        CriteriaBuilder builder = getRecordCriteriaBuilder();

        CriteriaQuery<Record> query = builder.createQuery(Record.class);
        Root<Record> recRoot = query.from(Record.class);
        query.select(recRoot);

        query.where(builder.equal(recRoot.get(Record_.pid), pid));

        return getRecord(query);
    }

    /**
     * Get a criteria builder for querying Records.
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getRecordCriteriaBuilder() {
        return recordDAO.getCriteriaBuilder();
    }

    /**
     * List all Records matching a built query.
     * @param query The query to match by.
     * @return A list of matching Records.
     */
    public List<Record> listRecords(CriteriaQuery<Record> query) {
        return recordDAO.list(query);
    }

    /**
     * List all Records.
     * @param offset     The offset.
     * @param maxResults The max number of records to fetch.
     * @return A list of Records.
     */
    public List<Record> listIterable(int offset, int maxResults) {
        return recordDAO.listIterable(offset, maxResults);
    }

    /**
     * Get a single Record matching a built query.
     * @param query The query to match by.
     * @return The matching Record.
     */
    public Record getRecord(CriteriaQuery<Record> query) {
        return recordDAO.get(query);
    }

    /**
     * List all Holdings matching a built query.
     * @param query The query to match by.
     * @return A list of matching Holdings.
     */
    public List<Holding> listHoldings(CriteriaQuery<Holding> query) {
        return holdingDAO.list(query);
    }

    /**
     * Retrieve the Holding matching the given Id.
     * @param id Id of the Holding to retrieve.
     * @return The Holding matching the Id.
     */
    public Holding getHoldingById(int id) {
        return holdingDAO.getById(id);
    }

    /**
     * Remove a Holding from the database.
     * @param obj Holding to remove.
     */
    public void removeHolding(Holding obj) {
        holdingDAO.remove(obj);
    }

    /**
     * Updates the external info of the given record, if necessary.
     * @param record      The record of which to update the external info.
     * @param hardRefresh Always update the external info.
     * @return Whether the record was updated.
     */
    public boolean updateExternalInfo(Record record, boolean hardRefresh) {
        try {
            // Do we need to update the external info?
            Integer days = deliveryProperties.getExternalInfoMinDaysCache();
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -days);

            Date lastUpdated = record.getExternalInfoUpdated();
            if (!hardRefresh && (lastUpdated != null) && lastUpdated.after(calendar.getTime()))
                return (record.getParent() != null) && updateExternalInfo(record.getParent(), hardRefresh);

            // We need to update the external info
            String pid = record.getPid();
            ExternalRecordInfo eri = lookup.getRecordMetaDataByPid(pid);
            List<ArchiveHoldingInfo> ahi = lookup.getArchiveHoldingInfoByPid(pid);
            Map<String, ExternalHoldingInfo> ehMap = lookup.getHoldingMetadataByPid(pid);

            // Update external record info
            if (record.getExternalInfo() != null) {
                record.getExternalInfo().mergeWith(eri);
            } else {
                record.setExternalInfo(eri);
            }

            // Update archive holding info
            record.setArchiveHoldingInfo(ahi);

            // Update the holdings, merge existing holdings, add new holdings, do not remove old holdings
            for (String signature : ehMap.keySet()) {
                boolean found = false;
                ExternalHoldingInfo ehi = ehMap.get(signature);

                for (Holding h : record.getHoldings()) {
                    if (signature.equals(h.getSignature())) {

                        h.checkSignature();

                        if (h.getExternalInfo() != null) {
                            h.getExternalInfo().mergeWith(ehi);
                        } else {
                            h.setExternalInfo(ehi);
                        }

                        found = true;
                    }
                }

                // Not found, but check again: maybe the signature changed, but the barcode is still the same
                if (!found) {
                    for (Holding h : record.getHoldings()) {
                        // Found a holding with a different signature, but with the same barcode, start a merge
                        if (ehi.getBarcode().equals(h.getExternalInfo().getBarcode())) {
                            h.setSignature(signature);
                            if (h.getExternalInfo() != null)
                                h.getExternalInfo().mergeWith(ehi);
                            else
                                h.setExternalInfo(ehi);

                            found = true;
                        }
                    }

                    // If still not found, create a new holding
                    if (!found) {
                        Holding holding = new Holding();
                        holding.setSignature(signature);
                        holding.setExternalInfo(ehi);
                        record.addHolding(holding);
                        holding.setRecord(record);
                    }
                }
            }

            record.setExternalInfoUpdated(new Date());
            return true;
        }
        catch (NoSuchPidException nspe) {
            // PID not found, or API down, then just skip the record
            return false;
        }
    }

    /**
     * Edit records.
     * @param newRecord The new record to put.
     * @param oldRecord The old record (or null if none).
     * @param result The binding result object to put the validation errors in.
     * @throws org.socialhistoryservices.delivery.api.NoSuchPidException Thrown when the
     * PID is not found in the external SRW API.
     * @throws org.socialhistoryservices.delivery.record.service.NoSuchParentException
     * Thrown when the provided record is detected as a pid by containing an
     * item separator (default .), but the parent record was not found in the
     * database.
     */
    public void createOrEdit(Record newRecord, Record oldRecord, BindingResult result)
            throws NoSuchParentException {
        String pid = newRecord.getPid();

        String itemSeparator = deliveryProperties.getItemSeparator();
        if (pid.contains(itemSeparator)) {
            String parentPid = pid.substring(0, pid.indexOf(itemSeparator));
            Record parent = getRecordByPid(parentPid);
            if (parent == null) {
                throw new NoSuchParentException();
            }
            newRecord.setParent(parent);
        }

        // Add holding/other API info if present
        updateExternalInfo(newRecord, true);

        // Validate the record.
        validateRecord(newRecord, result);

        // Add or save the record when no errors are present.
        if (!result.hasErrors()) {
            if (oldRecord == null) {
                addRecord(newRecord);
            } else {
                oldRecord.mergeWith(newRecord);
                saveRecord(oldRecord);
            }
        }
    }

    /**
     * Validate a record using the provided binding result to store errors.
     * @param record The record.
     * @param result The binding result.
     */
    private void validateRecord(Record record, BindingResult result) {
        // Validate the record
        mvcValidator.validate(record, result);

        // Validate associated holdings if present
        int i = 0;
        for(Holding h : record.getHoldings()) {
            // Set the record reference for newly created holdings (so you can
            // use the record reference without saving and loading them to
            // the database first).
            h.setRecord(record);
            result.pushNestedPath("holdings["+i+"]");
            mvcValidator.validate(h, result);
            result.popNestedPath();
            i++;
        }
    }

    /**
     * Create a record, using the metadata from the IISH API to populate its
     * fields.
     * @param pid The pid of the record (should exist in the API).
     * @return The new Record (not yet committed to the database).
     * @throws NoSuchPidException Thrown when the provided PID does not exist
     * in the API.
     */
    public Record createRecordByPid(String pid) throws NoSuchPidException {
        Record parent = null;
        String itemSeparator = deliveryProperties.getItemSeparator();
        if (pid.contains(itemSeparator)) {
            int idx = pid.indexOf(itemSeparator);
            String parentPid = pid.substring(0, idx);

            parent = getRecordByPid(parentPid);
            if (parent == null) {
                parent = createRecordByPid(parentPid);
                addRecord(parent);
            }
            else if (updateExternalInfo(parent, false)) {
                saveRecord(parent);
            }
        }

        Record r = new Record();
        r.setPid(pid);
        r.setExternalInfo(lookup.getRecordMetaDataByPid(pid));
        r.setArchiveHoldingInfo(lookup.getArchiveHoldingInfoByPid(pid));
        r.setParent(parent);
        List<Holding> hList = new ArrayList<>();
        for (Map.Entry<String, ExternalHoldingInfo> e :
            lookup.getHoldingMetadataByPid(pid).entrySet()) {
            Holding h = new Holding();
            h.setSignature(e.getKey());
            h.setExternalInfo(e.getValue());
            h.setRecord(r);
            hList.add(h);
        }
        r.setHoldings(hList);
        return r;
    }

    /**
     * Get all child records of the given record that are currently reserved.
     * @param record The parent record.
     * @return A list of all reserved child records.
     */
    public List<Record> getReservedChildRecords(Record record) {
        if (record.getParent() != null)
            return new ArrayList<>();

        CriteriaBuilder builder = getRecordCriteriaBuilder();
        CriteriaQuery<Record> query = builder.createQuery(Record.class);
        Root<Record> recRoot = query.from(Record.class);
        Join<Record, Holding> hRoot = recRoot.join(Record_.holdings);

        Predicate parentEquals = builder.equal(recRoot.get(Record_.parent), record);
        Predicate notAvailable = builder.notEqual(hRoot.get(Holding_.status), Holding.Status.AVAILABLE);

        query.select(recRoot);
        query.where(builder.and(parentEquals, notAvailable));

        return listRecords(query);
    }
}
