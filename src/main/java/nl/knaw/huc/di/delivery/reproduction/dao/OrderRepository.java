package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String>, OrderLegacyRepository {

}