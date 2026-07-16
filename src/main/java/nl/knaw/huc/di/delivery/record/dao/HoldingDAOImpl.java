package nl.knaw.huc.di.delivery.record.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalHoldingInfo;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access Object of the Holding data associated with a record.
 */
@Service
public class HoldingDAOImpl implements HoldingDAO {

    private final HoldingRepository holdingRepository;
    private final ExternalHoldingInfoRepository externalHoldingInfoRepository;

    public HoldingDAOImpl(HoldingRepository holdingRepository, ExternalHoldingInfoRepository externalHoldingInfoRepository) {
        this.holdingRepository = holdingRepository;
        this.externalHoldingInfoRepository = externalHoldingInfoRepository;
    }

    /**
     * Add a Holding to the database.
     *
     * @param obj Holding to add.
     */
    public void add(Holding obj) {
        holdingRepository.save(obj);
    }

    /**
     * Remove a Holding from the database.
     *
     * @param obj Holding to remove.
     */
    public void remove(Holding obj) {
        holdingRepository.delete(obj);
    }

    /**
     * Remove the ExternalHoldingInfo of a Holding from the database.
     *
     * @param obj Holding of which to remove the ExternalHoldingInfo.
     */
    public void removeExternalInfo(Holding obj) {
        ExternalHoldingInfo ehiObj = obj.getExternalInfo();
        externalHoldingInfoRepository.delete(ehiObj);
    }

    /**
     * Save changes to a Holding in the database.
     *
     * @param obj Holding to save.
     */
    public void save(Holding obj) {
        holdingRepository.save(obj);
    }

    /**
     * Retrieve the Holding matching the given Id.
     *
     * @param id Id of the Holding to retrieve.
     * @return The Holding matching the Id.
     */
    @Deprecated
    public Holding getById(int id) {
        return findById(id).orElse(null);
    }
    public Optional<Holding> findById(int id) {
        return holdingRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Holdings.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return holdingRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Holdings matching a built query.
     *
     * @param query The query to match by.
     * @return A list of matching Holdings.
     */
    public List<Holding> list(CriteriaQuery<Holding> query) {
        return holdingRepository.findAll(query);
    }

    /**
     * Get a single Holding matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Holding.
     */
    public Holding get(CriteriaQuery<Holding> query) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Optional<Holding> optionalHolding = holdingRepository.findAll(query, pageRequest).stream().findFirst();
        return optionalHolding.orElse(null);
    }

    @Override
    public Page<Holding> findAll(CriteriaQuery<Holding> query, Pageable pageable) {
        return holdingRepository.findAll(query, pageable);
    }
}
