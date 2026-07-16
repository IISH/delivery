package nl.knaw.huc.di.delivery.record.dao;

import nl.knaw.huc.di.delivery.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Integer>, RecordLegacyRepository {
}
