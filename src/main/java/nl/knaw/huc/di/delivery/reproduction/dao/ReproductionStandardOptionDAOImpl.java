package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionStandardOption;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a reproduction standard option.
 */
@Service
public class ReproductionStandardOptionDAOImpl implements ReproductionStandardOptionDAO {

    private final ReproductionStandardOptionRepository reproductionStandardOptionRepository;

    public ReproductionStandardOptionDAOImpl(ReproductionStandardOptionRepository reproductionStandardOptionRepository) {
        this.reproductionStandardOptionRepository = reproductionStandardOptionRepository;
    }

    /**
     * Add a ReproductionStandardOption to the database.
     *
     * @param obj ReproductionStandardOption to add.
     */
    public void add(ReproductionStandardOption obj) {
        reproductionStandardOptionRepository.save(obj);
    }

    /**
     * Remove a ReproductionStandardOption from the database.
     *
     * @param obj ReproductionStandardOption to remove.
     */
    public void remove(ReproductionStandardOption obj) {
        reproductionStandardOptionRepository.delete(obj);
    }

    /**
     * Save changes to a ReproductionStandardOption in the database.
     *
     * @param obj ReproductionStandardOption to save.
     */
    public void save(ReproductionStandardOption obj) {
        reproductionStandardOptionRepository.save(obj);
    }

    /**
     * Retrieve the ReproductionStandardOption matching the given Id.
     *
     * @param id Id of the ReproductionStandardOption to retrieve.
     * @return The ReproductionStandardOption matching the Id.
     */
    public ReproductionStandardOption getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<ReproductionStandardOption> findById(int id) {
        return reproductionStandardOptionRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying ReproductionStandardOptions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return reproductionStandardOptionRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all ReproductionStandardOptions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching ReproductionStandardOptions.
     */
    public List<ReproductionStandardOption> list(CriteriaQuery<ReproductionStandardOption> q) {
        return reproductionStandardOptionRepository.findAll(q);
    }

    /**
     * Get a single ReproductionStandardOption matching a built query.
     *
     * @param query The query to match by.
     * @return The matching ReproductionStandardOption.
     */
    public ReproductionStandardOption get(CriteriaQuery<ReproductionStandardOption> query) {
        Optional<ReproductionStandardOption> optionalRecord = reproductionStandardOptionRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
    }

    /**
     * List all ReproductionStandardOptions.
     *
     * @return A list of ReproductionStandardOptions.
     */
    public List<ReproductionStandardOption> listAll() {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<ReproductionStandardOption> cq = cb.createQuery(ReproductionStandardOption.class);
        Root<ReproductionStandardOption> root = cq.from(ReproductionStandardOption.class);

        cq.select(root);
        cq.orderBy(
                cb.asc(root.get("materialType")),
                cb.asc(root.get("price")),
                cb.asc(root.get("deliveryTime")));

        return reproductionStandardOptionRepository.findAll(cq);
    }
}
