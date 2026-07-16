package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a holding reservation.
 */
@Service
public class HoldingReservationDAOImpl implements HoldingReservationDAO {

    private final HoldingReservationRepository holdingReservationRepository;

    public HoldingReservationDAOImpl(HoldingReservationRepository holdingReservationRepository) {
        this.holdingReservationRepository = holdingReservationRepository;
    }

    /**
     * Get a criteria builder for querying HoldingReservations.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return holdingReservationRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q) {
        return holdingReservationRepository.findAll(q);
    }

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> list(CriteriaQuery<HoldingReservation> q, int firstResult, int maxResults) {
        return holdingReservationRepository.findAll(q);
    }

    /**
     * Count all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return The number of counted results.
     */
    public long count(CriteriaQuery<Long> q) {
//        return 0;
        throw  new UnsupportedOperationException("Not supported.");
    }

    /**
     * Retrieve the HoldingReservation matching the given ID.
     *
     * @param id ID of the HoldingReservation to retrieve.
     * @return The HoldingReservation matching the ID.
     */
    public HoldingReservation getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<HoldingReservation> findById(int id) {
        return holdingReservationRepository.findById(id);
    }

    @Override
    public Page<HoldingReservation> findAll(CriteriaQuery<HoldingReservation> cq, Pageable pageable) {
        return holdingReservationRepository.findAll(cq, pageable);
    }
}
