package nl.knaw.huc.di.delivery.request.service;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.Map;

/**
 * Request search helper class for tuple queries.
 *
 * @param <R> The request entity.
 */
public abstract class TupleRequestSearch<R> extends RequestSearch<R> {
    /**
     * Creates a new search helper.
     *
     * @param clazz The request class.
     * @param cb    The criteria builder.
     * @param p     The parameters from the user.
     */
    public TupleRequestSearch(Class<R> clazz, CriteriaBuilder cb, Map<String, String[]> p) {
        super(clazz, cb, p);
    }

    /**
     * Create a query that will create a tuple for the search results.
     *
     * @return A query for the persistence layer.
     */
    public CriteriaQuery<Tuple> tuple() {
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<R> hrRoot = cq.from(clazz);
        build(hrRoot, cq);
        return cq;
    }

    /**
     * Build the query.
     *
     * @param hrRoot The root entity.
     * @param cq     The query to build upon.
     */
    protected abstract void build(Root<R> hrRoot, CriteriaQuery<?> cq);
}
