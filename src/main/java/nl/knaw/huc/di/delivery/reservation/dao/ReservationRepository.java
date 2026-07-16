package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Integer>, ReservationLegacyRepository {
}
