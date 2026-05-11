package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionCustomNote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReproductionCustomNoteRepository extends JpaRepository<ReproductionCustomNote, Integer>, ReproductionCustomNoteLegacyRepository {

}