package nl.knaw.huc.di.delivery.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * CriteriaRepository is a bridge to sent criteriaQuery objects to a Spring boot 3 JPA repository.
 * This is a solution to legacy code that uses an entity manager directly to construct queries and filters.
 */
public interface LegacyRepository<T> {
    List<T> findAll(CriteriaQuery<T> criteriaQuery);
    Page<T> findAll(CriteriaQuery<T> criteriaQuery, Pageable pageable);
    List<T> findAll(CriteriaQuery<T> criteriaQuery, Specification<T> specification);
    Page<T> findAll(CriteriaQuery<T> criteriaQuery, Specification<T> specification, Pageable pageable);
    EntityManager getEntityManager();
}
