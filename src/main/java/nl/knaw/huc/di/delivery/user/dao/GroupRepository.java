package nl.knaw.huc.di.delivery.user.dao;

import nl.knaw.huc.di.delivery.user.entity.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, Integer> {}