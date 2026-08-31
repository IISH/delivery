package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.*;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access bject of a reservation.
 */
@Service
public class ReservationDAOImpl implements ReservationDAO {

    private final ReservationRepository reservationRepository;

    public ReservationDAOImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Add a Reservation to the database.
     *
     * @param obj Reservation to add.
     */
    public void add(Reservation obj) {
        reservationRepository.save(obj);
    }

    /**
     * Remove a Reservation from the database.
     *
     * @param obj Reservation to remove.
     */
    public void remove(Reservation obj) {
        reservationRepository.delete(obj);
    }

    /**
     * Save changes to a Reservation in the database.
     *
     * @param obj Reservation to save.
     */
    public Reservation save(Reservation obj) {
        return reservationRepository.save(obj);
    }

    /**
     * Retrieve the Reservation matching the given Id.
     *
     * @param id Id of the Reservation to retrieve.
     * @return The Reservation matching the Id.
     */
    public Reservation getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<Reservation> findById(int id) {
        return reservationRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Reservations.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return reservationRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Reservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reservations.
     */
    public List<Reservation> list(CriteriaQuery<Reservation> q) {
        return reservationRepository.findAll(q);
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    public List<Tuple> listForTuple(CriteriaQuery<Tuple> q) {
        return reservationRepository.getEntityManager().createQuery(q).getResultList();
    }

    /**
     * Get a single Reservation matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reservation.
     */
    public Reservation get(CriteriaQuery<Reservation> query) {
        Optional<Reservation> optionalRecord = reservationRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
    }

    /**
     * Get an active reservation relating to a specific Holding.
     *
     * @param h Holding to find a reservation for.
     * @return The active reservation, null if none exist.
     */
    public Reservation getActiveFor(Holding h) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
        Root<Reservation> resRoot = cq.from(Reservation.class);
        cq.select(resRoot);

        Join<Reservation, HoldingReservation> hrRoot = resRoot.join("holdingReservations");
        Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");

        cq.where(cb.and(
                cb.equal(hRoot.get("id"), h.getId()),
                cb.equal(hrRoot.get("completed"), false)
        ));
        cq.orderBy(cb.asc(resRoot.get("creationDate")));

        return reservationRepository.findAll(cq).stream().findFirst().orElse(null);
    }

    /**
     * Check whether the given record is linked to a pending reservation based on the container.
     *
     * @param record The record to check on.
     * @return Whether the given record is linked to a pending reservation based on the container.
     */
    public boolean hasPendingReservation(Record record) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
        Root<Reservation> resRoot = cq.from(Reservation.class);
        cq.select(resRoot);

        Join<Reservation, HoldingReservation> hrRoot = resRoot.join("holdingReservations");
        Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");
        Join<Holding, Record> rRoot = hRoot.join("record");
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join("externalInfo");

        cq.where(cb.and(
                cb.equal(resRoot.get("status"), Reservation.Status.PENDING),
                cb.equal(rRoot.get("parent"), record.getParent()),
                cb.isNotNull(eriRoot.get("container")),
                cb.equal(eriRoot.get("container"), record.getExternalInfo().getContainer())
        ));

        return !reservationRepository.findAll(cq).isEmpty();
    }
}
