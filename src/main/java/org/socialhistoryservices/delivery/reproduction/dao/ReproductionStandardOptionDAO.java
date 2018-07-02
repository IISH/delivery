package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.reproduction.entity.ReproductionStandardOption;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access object of a reproduction standard option.
 */
public interface ReproductionStandardOptionDAO {
	/**
	 * Add a ReproductionStandardOption to the database.
	 *
	 * @param obj ReproductionStandardOption to add.
	 */
    void add(ReproductionStandardOption obj);

	/**
	 * Remove a ReproductionStandardOption from the database.
	 *
	 * @param obj ReproductionStandardOption to remove.
	 */
    void remove(ReproductionStandardOption obj);

	/**
	 * Save changes to a ReproductionStandardOption in the database.
	 *
	 * @param obj ReproductionStandardOption to save.
	 */
    void save(ReproductionStandardOption obj);

	/**
	 * Retrieve the ReproductionStandardOption matching the given Id.
	 *
	 * @param id Id of the ReproductionStandardOption to retrieve.
	 * @return The ReproductionStandardOption matching the Id.
	 */
    ReproductionStandardOption getById(int id);

	/**
	 * Get a criteria builder for querying ReproductionStandardOptions.
	 *
	 * @return the CriteriaBuilder.
	 */
    CriteriaBuilder getCriteriaBuilder();

	/**
	 * List all ReproductionStandardOptions matching a built query.
	 *
	 * @param q The criteria query to execute
	 * @return A list of matching ReproductionStandardOptions.
	 */
    List<ReproductionStandardOption> list(CriteriaQuery<ReproductionStandardOption> q);

	/**
	 * Get a single ReproductionStandardOption matching a built query.
	 *
	 * @param query The query to match by.
	 * @return The matching ReproductionStandardOption.
	 */
    ReproductionStandardOption get(CriteriaQuery<ReproductionStandardOption> query);

	/**
	 * List all ReproductionStandardOptions.
	 *
	 * @return A list of ReproductionStandardOptions.
	 */
    List<ReproductionStandardOption> listAll();
}
