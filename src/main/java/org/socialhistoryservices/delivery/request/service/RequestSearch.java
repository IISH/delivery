package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.util.InvalidRequestException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Request search helper class, with support for paging.
 *
 * @param <R> The request entity.
 */
public abstract class RequestSearch<R> {
    private static final DateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    protected Class<R> clazz;
    protected CriteriaBuilder cb;
    protected Map<String, String[]> p;

    /**
     * Creates a new search helper.
     *
     * @param clazz The request class.
     * @param cb    The criteria builder.
     * @param p     The parameters from the user.
     */
    public RequestSearch(Class<R> clazz, CriteriaBuilder cb, Map<String, String[]> p) {
        this.clazz = clazz;
        this.cb = cb;
        this.p = p;
    }

    /**
     * Create a query that will list the search results.
     *
     * @return A query for the persistance layer.
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
     * @return A query for the persistance layer.
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

    /**
     * Returns a single date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getDateFilter(Map<String, String[]> p) {
        Date date = null;
        if (p.containsKey("date")) {
            try {
                date = API_DATE_FORMAT.parse(p.get("date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid date: " + p.get("date")[0]);
            }
        }
        return date;
    }

    /**
     * Returns a 'from' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getFromDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsFrom = p.containsKey("from_date") && !p.get("from_date")[0].trim().equals("");
        if (containsFrom) {
            try {
                date = API_DATE_FORMAT.parse(p.get("from_date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid from_date: " + p.get("from_date")[0]);
            }
        }
        return date;
    }

    /**
     * Returns a 'to' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getToDateFilter(Map<String, String[]> p) {
        Date date = null;
        boolean containsTo = p.containsKey("to_date") && !p.get("to_date")[0].trim().equals("");
        if (containsTo) {
            try {
                date = API_DATE_FORMAT.parse(p.get("to_date")[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid to_date: " + p.get("to_date")[0]);
            }
        }
        return date;
    }
}
