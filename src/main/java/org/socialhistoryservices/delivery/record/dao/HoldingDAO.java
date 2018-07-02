package org.socialhistoryservices.delivery.record.dao;

import org.socialhistoryservices.delivery.record.entity.Holding;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

/**
 * Interface representing the Data Access Object of the Holding data
 * associated with a record.
 */
public interface HoldingDAO {
    /**
     * Add a Holding to the database.
     * @param obj Holding to add.
     */
    void add(Holding obj);

    /**
     * Remove a Holding from the database.
     * @param obj Holding to remove.
     */
    void remove(Holding obj);

	/**
	 * Remove the ExternalHoldingInfo of a Holding from the database.
	 * @param obj Holding of which to remove the ExternalHoldingInfo.
	 */
    void removeExternalInfo(Holding obj);

    /**
     * Save changes to a Holding in the database.
     * @param obj Holding to save.
     */
    void save(Holding obj);

    /**
     * Retrieve the Holding matching the given Id.
     * @param id Id of the Holding to retrieve.
     * @return The Holding matching the Id.
     */
    Holding getById(int id);

    /**
     * Get a criteria builder for querying Holdings.
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Holdings matching a built query.
     * @param query The query to match by.
     * @return A list of matching Holdings.
     */
    List<Holding> list(CriteriaQuery<Holding> query);

    /**
     * Get a single Holding matching a built query.
     * @param query The query to match by.
     * @return The matching Holding.
     */
    Holding get(CriteriaQuery<Holding> query);
}

