package nl.knaw.huc.di.delivery.user.dao;

import nl.knaw.huc.di.delivery.user.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {
    java.util.Optional<User> findByUsername(String username);
    java.util.Optional<User> findByEmail(String username);
}