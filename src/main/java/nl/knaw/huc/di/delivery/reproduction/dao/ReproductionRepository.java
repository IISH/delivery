package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReproductionRepository extends JpaRepository<Reproduction, Integer>, ReproductionLegacyRepository {
}
