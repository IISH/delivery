package org.socialhistoryservices.delivery.reservation.dao;

import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

@Repository
public class ReservationDateExceptionDAOImpl implements ReservationDateExceptionDAO {
    private EntityManager entityManager;

    /**
     * Set the entity manager to use in this DAO, internal.
     * @param entityManager The manager.
     */
    @PersistenceContext
    private void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Add a Reservation to the database.
     * @param obj Reservation to add.
     */
    public void add(ReservationDateException obj){
        entityManager.persist(obj);
    }

    /**
     * Remove a Reservation from the database.
     * @param obj Reservation to remove.
     */
    public void remove(ReservationDateException obj){
        try{
            obj = entityManager.getReference(ReservationDateException.class, obj.getId());
            entityManager.remove(obj);
        } catch (EntityNotFoundException ignored) {}
    }

    /**
     * Save changes to a Reservation in the database.
     * @param obj Reservation to save.
     */
    public void save(ReservationDateException obj){
        entityManager.merge(obj);
    }

    /**
     * List all ReservationDateExceptions matching a built query.
     * @param q The criteria query to execute.
     * @return A list of matching ReservationDateExceptions.
     */
    public List<ReservationDateException> list(CriteriaQuery<ReservationDateException> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a criteria builder for querying ReservationDateExceptions.
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() { return entityManager.getCriteriaBuilder(); }

    /**
     * Get a ReservationDateException matching a given id.
     * @param id The id to match the ReservationDateException on.
     * @return A ReservationDateException matching the id.
     */
    public ReservationDateException getById(int id) {return entityManager.find(ReservationDateException.class, id); }
}
