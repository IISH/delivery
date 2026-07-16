package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface HoldingReservationRepository extends JpaRepository<HoldingReservation, Integer>, HoldingReservationLegacyRepository {
}
