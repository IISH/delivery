package org.socialhistoryservices.delivery.request.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Map;

/**
 * Request search helper class for list queries, with support for paging.
 *
 * @param <R> The request entity.
 */
public abstract class ListRequestSearch<R> extends RequestSearch<R> {
    /**
     * Creates a new search helper.
     *
     * @param clazz The request class.
     * @param cb    The criteria builder.
     * @param p     The parameters from the user.
     */
    public ListRequestSearch(Class<R> clazz, CriteriaBuilder cb, Map<String, String[]> p) {
        super(clazz, cb, p);
    }

    /**
     * Create a query that will list the search results.
     *
     * @return A query for the persistence layer.
     */
    public CriteriaQuery<R> list() {
        CriteriaQuery<R> cq = cb.createQuery(clazz);
        Root<R> hrRoot = cq.from(clazz);
        cq.select(hrRoot);
        build(hrRoot, cq, false);
        return cq;
    }

    /**
     * Create a query that will count all the search results.
     *
     * @return A query for the persistence layer.
     */
    public CriteriaQuery<Long> count() {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<R> hrRoot = cq.from(clazz);
        cq.select(cb.count(hrRoot));
        build(hrRoot, cq, true);
        return cq;
    }

    /**
     * Build the query.
     *
     * @param hrRoot  The root entity.
     * @param cq      The query to build upon.
     * @param isCount Whether the query is a count or not.
     */
    protected abstract void build(Root<R> hrRoot, CriteriaQuery<?> cq, boolean isCount);
}
