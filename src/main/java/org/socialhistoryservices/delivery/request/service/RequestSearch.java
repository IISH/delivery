package org.socialhistoryservices.delivery.request.service;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.socialhistoryservices.delivery.util.InvalidRequestException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Request search helper class.
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
     * Obtain a material filter predicate.
     *
     * @param materialTypeExpression The material type field.
     * @return The material predicate.
     */
    protected Predicate getMaterialPredicate(Expression<ExternalRecordInfo.MaterialType> materialTypeExpression) {
        String material = p.containsKey("material") ? p.get("material")[0].trim().toUpperCase() : "";
        if (!material.equals("")) {
            try {
                return cb.equal(materialTypeExpression, ExternalRecordInfo.MaterialType.valueOf(material));
            }
            catch (IllegalArgumentException ex) {
                throw new InvalidRequestException("No such material: " + material);
            }
        }
        return null;
    }

    /**
     * Obtain a date filter predicate.
     *
     * @param dateExpression The date field.
     * @param autoDate       Auto add the date of today.
     * @return The date predicate.
     */
    protected Predicate getDatePredicate(Expression<Date> dateExpression, boolean autoDate) {
        Date date = getDateFilter();
        if (date != null)
            return cb.equal(dateExpression, date);

        Predicate fromPredicate = null;
        Date fromDate = getFromDateFilter();
        fromDate = (autoDate && fromDate == null) ? new Date() : fromDate;
        if (fromDate != null)
            fromPredicate = cb.greaterThanOrEqualTo(dateExpression, fromDate);

        Predicate toPredicate = null;
        Date toDate = getToDateFilter();
        toDate = (autoDate && toDate == null) ? new Date() : toDate;
        if (toDate != null)
            toPredicate = cb.lessThanOrEqualTo(dateExpression, toDate);

        if (fromPredicate != null && toPredicate != null)
            return cb.and(fromPredicate, toPredicate);

        if (fromPredicate != null)
            return fromPredicate;

        if (toPredicate != null)
            return toPredicate;

        return null;
    }

    /**
     * Returns a single date from the parameter map.
     *
     * @return A date, if found.
     */
    private Date getDateFilter() {
        return getDateFilterForKey("date");
    }

    /**
     * Returns a 'from' date from the parameter map.
     *
     * @return A date, if found.
     */
    private Date getFromDateFilter() {
        return getDateFilterForKey("from_date");
    }

    /**
     * Returns a 'to' date from the parameter map.
     *
     * @return A date, if found.
     */
    private Date getToDateFilter() {
        return getDateFilterForKey("to_date");
    }

    /**
     * Obtains the date from the parameter map.
     *
     * @param key The date key.
     * @return A date, if found.
     */
    private Date getDateFilterForKey(String key) {
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
