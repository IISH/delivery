package nl.knaw.huc.di.delivery.reproduction.dao;

import jakarta.persistence.criteria.Root;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a holding reproduction.
 */
@Service
public class HoldingReproductionDAOImpl implements HoldingReproductionDAO {

    private final HoldingReproductionRepository holdingReproductionRepository;

    public HoldingReproductionDAOImpl(HoldingReproductionRepository holdingReproductionRepository) {
        this.holdingReproductionRepository = holdingReproductionRepository;
    }

    /**
     * Get a criteria builder for querying HoldingReproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return holdingReproductionRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q) {
        return holdingReproductionRepository.findAll(q);
    }

    /**
     * List all HoldingReproductions matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReproductions.
     */
    public List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q, int firstResult, int maxResults) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<HoldingReproduction> query = cb.createQuery(HoldingReproduction.class);

        Root<HoldingReproduction> rRoot = query.from(HoldingReproduction.class);
        query.select(rRoot);
        query.orderBy(cb.asc(rRoot.get("id")));

        Pageable pageable = Pageable.ofSize(firstResult);
        return holdingReproductionRepository.findAll(query, pageable).getContent();
    }

    /**
     * Count all HoldingReproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q) {
        return 0;
    }

    /**
     * Retrieve the HoldingReproduction matching the given ID.
     *
     * @param id ID of the HoldingReproduction to retrieve.
     * @return The HoldingReproduction matching the ID.
     */
    public HoldingReproduction getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<HoldingReproduction> findById(int id) {
        return holdingReproductionRepository.findById(id);
    }

    @Override
    public Page<HoldingReproduction> findAll(CriteriaQuery<HoldingReproduction> cq, Pageable pageable) {
        return holdingReproductionRepository.findAll(cq, pageable);
    }
}
