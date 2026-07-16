package nl.knaw.huc.di.delivery.permission.dao;

import nl.knaw.huc.di.delivery.permission.entity.Permission;
import nl.knaw.huc.di.delivery.record.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Integer>, PermissionLegacyRepository {

    boolean existsPermissionByRecord(Record record);
}