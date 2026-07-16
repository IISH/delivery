package nl.knaw.huc.di.delivery.record.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.record.entity.Record;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access Object of a Record.
 */
@Service
public class RecordDAOImpl implements RecordDAO {

    private final RecordRepository recordRepository;
    private final ExternalRecordInfoRepository externalRecordInfoRepository;


    public RecordDAOImpl(RecordRepository recordRepository, ExternalRecordInfoRepository externalRecordInfoRepository) {
        this.recordRepository = recordRepository;
        this.externalRecordInfoRepository = externalRecordInfoRepository;
    }

    /**
     * Add a Record to the database.
     *
     * @param obj Record to add.
     */
    public void add(Record obj) {
        recordRepository.save(obj);
    }

    /**
     * Remove a Record from the database.
     *
     * @param obj Record to remove.
     */
    public void remove(Record obj) {
        recordRepository.delete(obj);
    }

    /**
     * Remove the ExternalRecordInfo of a Record from the database.
     *
     * @param obj Record of which to remove the ExternalRecordInfo.
     */
    public void removeExternalInfo(Record obj) {
            ExternalRecordInfo eriObj = obj.getExternalInfo();
            externalRecordInfoRepository.delete(eriObj);
    }

    /**
     * Save changes to a Record in the database.
     *
     * @param obj Record to save.
     */
    public void save(Record obj) {
        recordRepository.save(obj);
    }

    /**
     * Retrieve the Record matching the given Id.
     *
     * @param id Id of the Record to retrieve.
     * @return The Record matching the Id.
     */
    @Deprecated
    public Record getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<Record> findById(int id) {
        return recordRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Records.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return recordRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Records matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Records.
     */
    public List<Record> list(CriteriaQuery<Record> query) {
        return recordRepository.findAll(query);
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
        query.orderBy(cb.asc(rRoot.get("id")));

        Pageable pageable = Pageable.ofSize(offset);
        return recordRepository.findAll(query, pageable).getContent();
    }

    /**
     * Get a single Record matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Record.
     */
    public Record get(CriteriaQuery<Record> query) {
        Optional<Record> optionalRecord = recordRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
    }
}
