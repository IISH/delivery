package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a reproduction.
 */
@Service
public class ReproductionDAOImpl implements ReproductionDAO {

    private final ReproductionRepository reproductionRepository;

    public ReproductionDAOImpl(ReproductionRepository reproductionRepository) {
        this.reproductionRepository = reproductionRepository;
    }

    /**
     * Add a Reproduction to the database.
     *
     * @param obj Reproduction to add.
     */
    public synchronized void add(Reproduction obj) {
        reproductionRepository.save(obj);
    }

    /**
     * Remove a Reproduction from the database.
     *
     * @param obj Reproduction to remove.
     */
    public void remove(Reproduction obj) {
        reproductionRepository.delete(obj);
    }

    /**
     * Save changes to a Reproduction in the database.
     *
     * @param obj Reproduction to save.
     * @return The Reproduction saved
     */
    public Reproduction save(Reproduction obj) {
        // On save, cascading does not work for new holdings
        return reproductionRepository.save(obj);
    }

    /**
     * Retrieve the Reproduction matching the given Id.
     *
     * @param id Id of the Reproduction to retrieve.
     * @return The Reproduction matching the Id.
     */
    public Reproduction getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<Reproduction> findById(int id) {
        return reproductionRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Reproductions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return reproductionRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Reproductions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reproductions.
     */
    public List<Reproduction> list(CriteriaQuery<Reproduction> q) {
        return reproductionRepository.findAll(q);
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    @Deprecated
    public List<Tuple> listForTuple(CriteriaQuery<Tuple> q) {
        return reproductionRepository.getEntityManager().createQuery(q).getResultList();
    }

    /**
     * Get a single Reproduction matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reproduction.
     */
    public Reproduction get(CriteriaQuery<Reproduction> query) {
        Optional<Reproduction> optionalRecord = reproductionRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
    }

    /**
     * Get an active reproduction relating to a specific Holding.
     *
     * @param h Holding to find a reproduction for.
     * @return The active reproduction, null if none exist.
     */
    public Reproduction getActiveFor(Holding h) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reproduction> cq = cb.createQuery(Reproduction.class);
        Root<Reproduction> rRoot = cq.from(Reproduction.class);
        cq.select(rRoot);

        Join<Reproduction, HoldingReproduction> hrRoot = rRoot.join("holdingReproductions");
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join("holding");
        Expression<Boolean> where = cb.equal(hRoot.get("id"), h.getId());
        where = cb.and(where, cb.equal(hrRoot.get("completed"), false));

        cq.where(where);
        cq.orderBy(cb.asc(rRoot.get("creationDate")));

        return reproductionRepository.findAll(cq).stream().findFirst().orElse(null);
    }

    /**
     * Check whether there are any reproductions made on the holding.
     *
     * @param h Holding to check for reproductions for.
     * @return Whether any reproductions have been made including this holding.
     */
    public boolean hasReproductions(Holding h) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reproduction> cq = cb.createQuery(Reproduction.class);
        Root<Reproduction> resRoot = cq.from(Reproduction.class);
        cq.select(resRoot);

        Join<Reproduction, HoldingReproduction> hrRoot = resRoot.join("holdingReproductions");
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join("holding");
        Expression<Boolean> where = cb.equal(hRoot.get("id"), h.getId());
        cq.where(where);

        return !reproductionRepository.findAll(cq).isEmpty();
    }
}
