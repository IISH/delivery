package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionCustomNote;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a reproduction standard option.
 */
@Service
public class ReproductionCustomNoteDAOImpl implements ReproductionCustomNoteDAO {

    private final ReproductionCustomNoteRepository reproductionCustomNoteRepository;

    public ReproductionCustomNoteDAOImpl(ReproductionCustomNoteRepository reproductionCustomNoteRepository) {
        this.reproductionCustomNoteRepository = reproductionCustomNoteRepository;
    }

    /**
     * Add a ReproductionCustomNote to the database.
     *
     * @param obj ReproductionCustomNote to add.
     */
    public void add(ReproductionCustomNote obj) {
        reproductionCustomNoteRepository.save(obj);
    }

    /**
     * Remove a ReproductionCustomNote from the database.
     *
     * @param obj ReproductionCustomNote to remove.
     */
    public void remove(ReproductionCustomNote obj) {
        reproductionCustomNoteRepository.delete(obj);
    }

    /**
     * Save changes to a ReproductionCustomNote in the database.
     *
     * @param obj ReproductionCustomNote to save.
     */
    public void save(ReproductionCustomNote obj) {
        reproductionCustomNoteRepository.save(obj);
    }

    /**
     * Retrieve the ReproductionCustomNote matching the given Id.
     *
     * @param id Id of the ReproductionCustomNote to retrieve.
     * @return The ReproductionCustomNote matching the Id.
     */
    public ReproductionCustomNote getById(int id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<ReproductionCustomNote> findById(int id) {
        return reproductionCustomNoteRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying ReproductionCustomNotes.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return reproductionCustomNoteRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all ReproductionCustomNotes matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching ReproductionCustomNotes.
     */
    public List<ReproductionCustomNote> list(CriteriaQuery<ReproductionCustomNote> q) {
        return reproductionCustomNoteRepository.findAll(q);
    }

    /**
     * Get a single ReproductionCustomNote matching a built query.
     *
     * @param query The query to match by.
     * @return The matching ReproductionCustomNote.
     */
    public ReproductionCustomNote get(CriteriaQuery<ReproductionCustomNote> query) {
        Optional<ReproductionCustomNote> optionalRecord = reproductionCustomNoteRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
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
