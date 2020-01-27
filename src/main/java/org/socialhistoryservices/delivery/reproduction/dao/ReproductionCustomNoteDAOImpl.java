package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.reproduction.entity.ReproductionCustomNote;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Data Access object of a reproduction standard option.
 */
@Repository
public class ReproductionCustomNoteDAOImpl implements ReproductionCustomNoteDAO {
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
     * Add a ReproductionCustomNote to the database.
     *
     * @param obj ReproductionCustomNote to add.
     */
    public void add(ReproductionCustomNote obj) {
        entityManager.persist(obj);
    }

    /**
     * Remove a ReproductionCustomNote from the database.
     *
     * @param obj ReproductionCustomNote to remove.
     */
    public void remove(ReproductionCustomNote obj) {
        try {
            obj = entityManager.getReference(ReproductionCustomNote.class, obj.getId());
            entityManager.remove(obj);
        }
        catch (EntityNotFoundException ignored) {
        }
    }

    /**
     * Save changes to a ReproductionCustomNote in the database.
     *
     * @param obj ReproductionCustomNote to save.
     */
    public void save(ReproductionCustomNote obj) {
        entityManager.merge(obj);
    }

    /**
     * Retrieve the ReproductionCustomNote matching the given Id.
     *
     * @param id Id of the ReproductionCustomNote to retrieve.
     * @return The ReproductionCustomNote matching the Id.
     */
    public ReproductionCustomNote getById(int id) {
        return entityManager.find(ReproductionCustomNote.class, id);
    }

    /**
     * Get a criteria builder for querying ReproductionCustomNotes.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    /**
     * List all ReproductionCustomNotes matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching ReproductionCustomNotes.
     */
    public List<ReproductionCustomNote> list(CriteriaQuery<ReproductionCustomNote> q) {
        return entityManager.createQuery(q).getResultList();
    }

    /**
     * Get a single ReproductionCustomNote matching a built query.
     *
     * @param query The query to match by.
     * @return The matching ReproductionCustomNote.
     */
    public ReproductionCustomNote get(CriteriaQuery<ReproductionCustomNote> query) {
        try {
            TypedQuery<ReproductionCustomNote> q = entityManager.createQuery(query);
            q.setMaxResults(1);
            return q.getSingleResult();
        }
        catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * List all ReproductionCustomNotes by material type.
     *
     * @return A list of ReproductionCustomNotes.
     */
    public List<ReproductionCustomNote> listAll() {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<ReproductionCustomNote> cq = cb.createQuery(ReproductionCustomNote.class);
        Root<ReproductionCustomNote> root = cq.from(ReproductionCustomNote.class);
        cq.select(root);

        List<ReproductionCustomNote> storedCustomNotes = list(cq);
        List<ReproductionCustomNote> customNotes = new ArrayList<>();
        for (ExternalRecordInfo.MaterialType materialType : ExternalRecordInfo.MaterialType.values()) {
            boolean has = false;
            for (ReproductionCustomNote storedCustomNote : storedCustomNotes) {
                if (!has && materialType.equals(storedCustomNote.getMaterialType())) {
                    has = true;
                    customNotes.add(storedCustomNote);
                }
            }

            if (!has) {
                ReproductionCustomNote customNote = new ReproductionCustomNote();
                customNote.setMaterialType(materialType);
                customNotes.add(customNote);
            }
        }

        return customNotes;
    }
}
