package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.Order;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.Optional;

/**
 * Interface representing the Data Access object of an order.
 */
public interface OrderDAO {
    /**
     * Add a Order to the database.
     *
     * @param obj Order to add.
     */
    void add(Order obj);

    /**
     * Remove a Order from the database.
     *
     * @param obj Order to remove.
     */
    void remove(Order obj);

    /**
     * Save changes to a Order in the database.
     *
     * @param obj Order to save.
     */
    void save(Order obj);

    /**
     * Retrieve the Order matching the given Id.
     *
     * @param id Id of the Order to retrieve.
     * @return The Order matching the Id.
     */
    @Deprecated
    Order getById(String id);
    Optional<Order> findById(String id);

    /**
     * Get a criteria builder for querying Orders.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * List all Orders matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Orders.
     */
    List<Order> list(CriteriaQuery<Order> q);

    /**
     * Get a single Order matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Order.
     */
    Order get(CriteriaQuery<Order> query);
}
