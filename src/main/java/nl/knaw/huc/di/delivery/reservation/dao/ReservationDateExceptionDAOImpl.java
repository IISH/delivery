package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.ReservationDateException;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationDateExceptionDAOImpl implements ReservationDateExceptionDAO {

    private final ReservationDateExceptionRepository reservationDateExceptionRepository;

    public ReservationDateExceptionDAOImpl(ReservationDateExceptionRepository reservationDateExceptionRepository) {
        this.reservationDateExceptionRepository = reservationDateExceptionRepository;
    }

    /**
     * Add a Reservation to the database.
     *
     * @param obj Reservation to add.
     */
    public void add(ReservationDateException obj) {
        reservationDateExceptionRepository.save(obj);
    }

    /**
     * Remove a Reservation from the database.
     *
     * @param obj Reservation to remove.
     */
    public void remove(ReservationDateException obj) {
        reservationDateExceptionRepository.delete(obj);
    }

    /**
     * Save changes to a Reservation in the database.
     *
     * @param obj Reservation to save.
     */
    public void save(ReservationDateException obj) {
        reservationDateExceptionRepository.save(obj);
    }

    /**
     * List all ReservationDateExceptions matching a built query.
     *
     * @param q The criteria query to execute.
     * @return A list of matching ReservationDateExceptions.
     */
    @Deprecated
    public List<ReservationDateException> list(CriteriaQuery<ReservationDateException> q) {
        return reservationDateExceptionRepository.findAll(q);
    }

    @Override
    public Page<ReservationDateException> findAll(CriteriaQuery<ReservationDateException> q, Pageable pageable) {
        return reservationDateExceptionRepository.findAll(q, pageable);
    }

    /**
     * Get a criteria builder for querying ReservationDateExceptions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return reservationDateExceptionRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * Get a ReservationDateException matching a given id.
     *
     * @param id The id to match the ReservationDateException on.
     * @return A ReservationDateException matching the id.
     */
    public ReservationDateException getById(int id) {
        return findById(id).orElse(null);
    }
    public Optional<ReservationDateException> findById(int id) {
        return reservationDateExceptionRepository.findById(id);
    }
}
