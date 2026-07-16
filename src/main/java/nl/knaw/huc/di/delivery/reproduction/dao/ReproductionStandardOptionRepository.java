package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionStandardOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReproductionStandardOptionRepository extends JpaRepository<ReproductionStandardOption, Integer>, ReproductionStandardOptionLegacyRepository{
}
