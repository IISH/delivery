package nl.knaw.huc.di.delivery.record.dao;

import nl.knaw.huc.di.delivery.record.entity.ExternalHoldingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExternalHoldingInfoRepository extends JpaRepository<ExternalHoldingInfo, Integer>, ExternalHoldingInfoLegacyRepository {}