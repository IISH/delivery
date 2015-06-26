package org.socialhistoryservices.delivery.reproduction.dao;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access object of a reproduction.
 */
public interface ReproductionDAO {
	/**
	 * Add a Reproduction to the database.
	 *
	 * @param obj Reproduction to add.
	 */
	public void add(Reproduction obj);

	/**
	 * Remove a Reproduction from the database.
	 *
	 * @param obj Reproduction to remove.
	 */
	public void remove(Reproduction obj);

	/**
	 * Save changes to a Reproduction in the database.
	 *
	 * @param obj Reproduction to save.
	 */
	public void save(Reproduction obj);

	/**
	 * Retrieve the Reproduction matching the given Id.
	 *
	 * @param id Id of the Reproduction to retrieve.
	 * @return The Reproduction matching the Id.
	 */
	public Reproduction getById(int id);

	/**
	 * Get a criteria builder for querying Reproductions.
	 *
	 * @return the CriteriaBuilder.
	 */
	public CriteriaBuilder getCriteriaBuilder();

	/**
	 * List all Reproductions matching a built query.
	 *
	 * @param q The criteria query to execute
	 * @return A list of matching Reproductions.
	 */
	public List<Reproduction> list(CriteriaQuery<Reproduction> q);

	/**
	 * List all Tuples matching a built query.
	 *
	 * @param q The criteria query to execute
	 * @return A list of matching Tuples.
	 */
	public List<Tuple> listForTuple(CriteriaQuery<Tuple> q);

	/**
	 * Get a single Reproduction matching a built query.
	 *
	 * @param query The query to match by.
	 * @return The matching Reproduction.
	 */
	public Reproduction get(CriteriaQuery<Reproduction> query);

	/**
	 * Get an active reproduction relating to a specific Holding.
	 *
	 * @param h Holding to find a reproduction for.
	 * @return The active reproduction, null if none exist.
	 */
	public Reproduction getActiveFor(Holding h);

	/**
	 * Check whether there are any reproductions made on the holding.
	 *
	 * @param h Holding to check for reproductions for.
	 * @return Whether any reproductions have been made including this holding.
	 */
	public boolean hasReproductions(Holding h);
}
