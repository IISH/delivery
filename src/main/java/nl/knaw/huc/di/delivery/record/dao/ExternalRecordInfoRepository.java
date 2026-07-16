package nl.knaw.huc.di.delivery.record.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalRecordInfoRepository extends JpaRepository<ExternalRecordInfo, Integer>, ExternalRecordInfoLegacyRepository {}