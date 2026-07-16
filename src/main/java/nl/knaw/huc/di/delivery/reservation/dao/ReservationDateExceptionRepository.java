package nl.knaw.huc.di.delivery.reservation.dao;

import nl.knaw.huc.di.delivery.reservation.entity.ReservationDateException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationDateExceptionRepository extends JpaRepository<ReservationDateException, Integer>, ReservationDateExceptionLegacyRepository { }
