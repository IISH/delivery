package nl.knaw.huc.di.delivery.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.sqm.tree.select.SqmSelectStatement;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

public abstract class AbstractLegacyRepository<T> implements LegacyRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<T> findAll(CriteriaQuery<T> criteriaQuery) {
        final Specification<T> specification = Specification.unrestricted();
        return findAll(criteriaQuery, specification);
    }

    @Override
    public Page<T> findAll(CriteriaQuery<T> criteriaQuery, Pageable pageable) {
        final Specification<T> specification = Specification.unrestricted();
        return findAll(criteriaQuery, specification, pageable);
    }

    @Override
    public List<T> findAll(CriteriaQuery<T> criteriaQuery, Specification<T> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        // 1. Hook into the existing Root from legacy code
        @SuppressWarnings("unchecked")
        Root<T> root = (Root<T>) criteriaQuery.getRoots().iterator().next();
        // 2. Merge Specification with Legacy WHERE clause
        Predicate specPredicate = specification.toPredicate(root, criteriaQuery, cb);
        if (specPredicate != null) {
            Predicate existing = criteriaQuery.getRestriction();
            criteriaQuery.where(existing != null ? cb.and(existing, specPredicate) : specPredicate);
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    @Override
    public Page<T> findAll(CriteriaQuery<T> criteriaQuery, Specification<T> specification, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        // 1. Hook into the existing Root from legacy code
        @SuppressWarnings("unchecked")
        Root<T> root = (Root<T>) criteriaQuery.getRoots().iterator().next();
        // 2. Merge Specification with Legacy WHERE clause
        Predicate specPredicate = specification.toPredicate(root, criteriaQuery, cb);
        if (specPredicate != null) {
            Predicate existing = criteriaQuery.getRestriction();
            criteriaQuery.where(existing != null ? cb.and(existing, specPredicate) : specPredicate);
        }

        // 3. Apply Sorting from the Pageable object
        if (pageable.getSort().isSorted()) {
            criteriaQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));
        }

        // 4. Build the executable TypedQuery with Pagination limits
        TypedQuery<T> typedQuery = entityManager.createQuery(criteriaQuery);
        if (pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        // 5. Calculate Total Count for the Page object
        long total = getTotalCount(criteriaQuery);

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private long getTotalCount(CriteriaQuery<T> legacyQuery) {
        // Cast to Hibernate's internal representation
        SqmSelectStatement<T> sqmSelect = (org.hibernate.query.sqm.tree.select.SqmSelectStatement<T>) legacyQuery;
        SqmSelectStatement<Long> countQuery = sqmSelect.createCountQuery();
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
