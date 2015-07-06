package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;

/**
 * Represents the Data Access object of a holding reproduction.
 */
@Repository
public class HoldingReproductionDAOImpl implements HoldingReproductionDAO {
	private EntityManager entityManager;

	/**
	 * Set the entity manager to use in this DAO, internal.
	 *
	 * @param entityManager The manager.
	 */
	@PersistenceContext
	private void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * Get a criteria builder for querying HoldingReproductions.
	 *
	 * @return the CriteriaBuilder.
	 */
	public CriteriaBuilder getCriteriaBuilder() {
		return entityManager.getCriteriaBuilder();
	}

	/**
	 * List all HoldingReproductions matching a built query.
	 *
	 * @param q The criteria query to execute
	 * @return A list of matching HoldingReproductions.
	 */
	public List<HoldingReproduction> list(CriteriaQuery<HoldingReproduction> q) {
		return entityManager.createQuery(q).getResultList();
	}
}