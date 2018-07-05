package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.util.InvalidRequestException;

import javax.persistence.criteria.CriteriaBuilder;
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
     * Returns a single date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getDateFilter(Map<String, String[]> p) {
        return getDateFilterForKey("date", p);
    }

    /**
     * Returns a 'from' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getFromDateFilter(Map<String, String[]> p) {
        return getDateFilterForKey("from_date", p);
    }

    /**
     * Returns a 'to' date from the parameter map.
     *
     * @param p The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    protected Date getToDateFilter(Map<String, String[]> p) {
        return getDateFilterForKey("to_date", p);
    }

    /**
     * Obtains the date from the parameter map.
     *
     * @param key The date key.
     * @param p   The parameter map to search the given filter value in.
     * @return A date, if found.
     */
    private Date getDateFilterForKey(String key, Map<String, String[]> p) {
        Date date = null;
        boolean containsTo = p.containsKey(key) && !p.get(key)[0].trim().equals("");
        if (containsTo) {
            try {
                date = API_DATE_FORMAT.parse(p.get(key)[0]);
            }
            catch (ParseException ex) {
                throw new InvalidRequestException("Invalid " + key + ": " + p.get(key)[0]);
            }
        }
        return date;
    }
}
