package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingReproductionRepository extends JpaRepository<HoldingReproduction, Integer>, HoldingReproductionLegacyRepository {}

