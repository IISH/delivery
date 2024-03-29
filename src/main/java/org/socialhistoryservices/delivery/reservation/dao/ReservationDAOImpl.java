package org.socialhistoryservices.delivery.reservation.dao;

import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation_;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation_;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * Represents the Data Access bject of a reservation.
 */
@Repository
public class ReservationDAOImpl implements ReservationDAO {
    private EntityManager entityManager;

    /**
     * Set the entity manager to use in this DAO, internal.
     *
     * @param entityManager The manager.
     */
    @PersistenceContext
    private void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Add a Reservation to the database.
     *
     * @param obj Reservation to add.
     */
    public synchronized void add(Reservation obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a Reservation from the database.
     *
     * @param obj Reservation to remove.
     */
    public void remove(Reservation obj) {
        try {
            obj = entityManager.getReference(Reservation.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a Reservation in the database.
     *
     * @param obj Reservation to save.
     */
    public Reservation save(Reservation obj) {
        return entityManager.merge(obj);
    }

    /**
     * Retrieve the Reservation matching the given Id.
     *
     * @param id Id of the Reservation to retrieve.
     * @return The Reservation matching the Id.
     */
    public Reservation getById(int id) {
        return entityManager.find(Reservation.class, id);
    }

    /**
     * Get a criteria builder for querying Reservations.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all Reservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reservations.
     */
    public List<Reservation> list(CriteriaQuery<Reservation> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    public List<Tuple> listForTuple(CriteriaQuery<Tuple> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a single Reservation matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reservation.
     */
    public Reservation get(CriteriaQuery<Reservation> query) {
        try {
            TypedQuery<Reservation> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
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

        Join<Reservation, HoldingReservation> hrRoot = resRoot.join(Reservation_.holdingReservations);
        Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);

        cq.where(cb.and(
                cb.equal(hRoot.get(Holding_.id), h.getId()),
                cb.equal(hrRoot.get(HoldingReservation_.completed), false)
        ));
        cq.orderBy(cb.asc(resRoot.get(Reservation_.creationDate)));

        try {
            TypedQuery<Reservation> q = entityManager.createQuery(cq);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
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

        Join<Reservation, HoldingReservation> hrRoot = resRoot.join(Reservation_.holdingReservations);
        Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);
        Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join(Record_.externalInfo);

        cq.where(cb.and(
                cb.equal(resRoot.get(Reservation_.status), Reservation.Status.PENDING),
                cb.equal(rRoot.get(Record_.parent), record.getParent()),
                cb.isNotNull(eriRoot.get(ExternalRecordInfo_.container)),
                cb.equal(eriRoot.get(ExternalRecordInfo_.container), record.getExternalInfo().getContainer())
        ));

        try {
            TypedQuery<Reservation> q = entityManager.createQuery(cq);
            q.setMaxResults(1);
            return q.getSingleResult() != null;
        }
        catch (NoResultException ex) {
            return false;
        }
    }
}
