package nl.knaw.huc.di.delivery.reproduction.dao;

import nl.knaw.huc.di.delivery.reproduction.entity.Order;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Represents the Data Access object of a order.
 */
@Service
public class OrderDAOImpl implements OrderDAO {

    private final OrderRepository orderRepository;
    public OrderDAOImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Add a Order to the database.
     *
     * @param obj Order to add.
     */
    public void add(Order obj) {
        orderRepository.save(obj);
    }

    /**
     * Remove a Order from the database.
     *
     * @param obj Order to remove.
     */
    public void remove(Order obj) {
        orderRepository.delete(obj);
    }

    /**
     * Save changes to a Order in the database.
     *
     * @param obj Order to save.
     */
    public void save(Order obj) {
        orderRepository.save(obj);
    }

    /**
     * Retrieve the Order matching the given Id.
     *
     * @param id Id of the Order to retrieve.
     * @return The Order matching the Id.
     */
    public Order getById(String id) {
        return findById(id).orElse(null);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderRepository.findById(id);
    }

    /**
     * Get a criteria builder for querying Orders.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return orderRepository.getEntityManager().getCriteriaBuilder();
    }

    /**
     * List all Orders matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Orders.
     */
    public List<Order> list(CriteriaQuery<Order> q) {
        return orderRepository.findAll(q);
    }

    /**
     * Get a single Order matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Order.
     */
    public Order get(CriteriaQuery<Order> query) {
        Optional<Order> optionalRecord = orderRepository.findAll(query).stream().findFirst();
        return optionalRecord.orElse(null);
    }
}
