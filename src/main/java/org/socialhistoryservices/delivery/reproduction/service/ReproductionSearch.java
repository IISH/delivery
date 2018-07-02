package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.InvalidRequestException;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction_;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction_;
import org.socialhistoryservices.delivery.request.service.RequestSearch;

import javax.persistence.criteria.*;
import java.util.Date;
import java.util.Map;

/**
 * Reproduction search helper class, with support for paging.
 */
public class ReproductionSearch extends RequestSearch<HoldingReproduction> {

    /**
     * Creates a new reproduction search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReproductionSearch(CriteriaBuilder cb, Map<String, String[]> p) {
        super(HoldingReproduction.class, cb, p);
    }

    /**
     * Build the query.
     *
     * @param rpRoot  The root entity.
     * @param cq      The query to build upon.
     * @param isCount Whether the query is a count or not.
     */
    @Override
    protected void build(Root<HoldingReproduction> hrRoot, CriteriaQuery<?> cq, boolean isCount) {
        Join<HoldingReproduction, Reproduction> rRoot = hrRoot.join(HoldingReproduction_.reproduction);

        // Expression to be the where clause of the query
        Expression<Boolean> where = null;

        // Filters
        where = addDateFilter(p, cb, rRoot, where);
        where = addNameFilter(p, cb, rRoot, where);
        where = addEmailFilter(p, cb, rRoot, where);
        where = addStatusFilter(p, cb, rRoot, where);
        where = addPrintedFilter(p, cb, hrRoot, where);
        where = addSearchFilter(p, cb, hrRoot, rRoot, where);

        // Set the where clause
        if (where != null)
            cq.where(where);

        Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);

        if (!isCount) {
            cq.orderBy(parseSortFilter(p, cb, hrRoot, rRoot, hRoot));
        }
    }

    /**
     * Add the date/from_date/to_date filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addDateFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                              Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        Date date = getDateFilter(p);
        if (date != null) {
            Expression<Boolean> exDate = cb.between(rRoot.<Date>get(Reproduction_.date), date, date);
            where = (where != null) ? cb.and(where, exDate) : exDate;
        }
        else {
            Date fromDate = getFromDateFilter(p);
            Date toDate = getToDateFilter(p);
            if (fromDate != null) {
                Expression<Boolean> exDate = cb.greaterThanOrEqualTo(rRoot.<Date>get(Reproduction_.date), fromDate);
                where = (where != null) ? cb.and(where, exDate) : exDate;
            }
            if (toDate != null) {
                Expression<Boolean> exDate = cb.lessThanOrEqualTo(rRoot.<Date>get(Reproduction_.date), toDate);
                where = (where != null) ? cb.and(where, exDate) : exDate;
            }
        }
        return where;
    }

    /**
     * Add the name filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addNameFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                              Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        if (p.containsKey("customerName")) {
            Expression<Boolean> exName = cb.like(rRoot.<String>get(Reproduction_.customerName),
                "%" + p.get("customerName")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exName) : exName;
        }
        return where;
    }

    /**
     * Add the email filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addEmailFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                               Join<HoldingReproduction, Reproduction> rRoot,
                                               Expression<Boolean> where) {
        if (p.containsKey("customerEmail")) {
            Expression<Boolean> exEmail = cb.like(rRoot.get(Reproduction_.customerEmail),
                "%" + p.get("customerEmail")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exEmail) : exEmail;
        }
        return where;
    }

    /**
     * Add the status filter to the where clause, if present.
     *
     * @param p     The parameter list to search the given filter value in.
     * @param cb    The criteria builder.
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addStatusFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("status")) {
            String status = p.get("status")[0].trim().toUpperCase();
            if (!status.equals("")) { // Tolerant to empty status
                try {
                    Expression<Boolean> exStatus = cb.equal(rRoot.<Reproduction.Status>get(Reproduction_.status),
                        Reproduction.Status.valueOf(status));
                    where = (where != null) ? cb.and(where, exStatus) : exStatus;
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such status: " + status);
                }
            }
        }
        return where;
    }

    /**
     * Add the printed filter to the where clause, if present.
     *
     * @param p      The parameter list to search the given filter value in.
     * @param cb     The criteria builder.
     * @param hrRoot The holding reproduction root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addPrintedFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                 Root<HoldingReproduction> hrRoot,
                                                 Expression<Boolean> where) {
        if (p.containsKey("printed")) {
            String printed = p.get("printed")[0].trim().toLowerCase();
            if (printed.isEmpty()) {
                return where;
            }

            Expression<Boolean> exPrinted = cb.equal(hrRoot.<Boolean>get(HoldingReproduction_.printed),
                Boolean.parseBoolean(p.get("printed")[0]));
            where = (where != null) ? cb.and(where, exPrinted) : exPrinted;
        }
        return where;
    }

    /**
     * Add the search filter to the where clause, if present.
     *
     * @param p      The parameter list to search the given filter value in.
     * @param cb     The criteria builder.
     * @param hrRoot The holding reproduction root.
     * @param rRoot  The reproduction root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addSearchFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                Root<HoldingReproduction> hrRoot,
                                                Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("search") && !p.get("search")[0].trim().equals("")) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);
            Join<Holding, Record> recRoot = hRoot.join(Holding_.record);
            Join<Record, ExternalRecordInfo> eRoot = recRoot.join(Record_.externalInfo);
            Join<Record, Record> prRoot = recRoot.join(Record_.parent, JoinType.LEFT);
            Join<Record, Holding> phRoot = prRoot.join(Record_.holdings, JoinType.LEFT);

            Expression<Boolean> exSearch = cb.or(
                cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)), "%" + search + "%"),
                cb.like(cb.lower(rRoot.<String>get(Reproduction_.customerName)), "%" + search + "%"),
                cb.like(cb.lower(rRoot.<String>get(Reproduction_.customerEmail)), "%" + search + "%"),
                cb.like(cb.lower(hRoot.<String>get(Holding_.signature)), "%" + search + "%"),
                cb.like(cb.lower(phRoot.<String>get(Holding_.signature)), "%" + search + "%")
            );

            where = (where != null) ? cb.and(where, exSearch) : exSearch;
        }
        return where;
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query.
     *
     * @param p      The parameter list to search the filter values in.
     * @param cb     The criteria builder used to construct the Order.
     * @param hrRoot The root of the reproduction holding used to construct the Order.
     * @param rRoot  The root of the reproduction used to construct the Order.
     * @param hRoot  The root of the holding used to construct the Order.
     * @return The order the query should be in (asc/desc) sorted on provided column. Defaults to asc on the PK column.
     */
    private javax.persistence.criteria.Order parseSortFilter(Map<String, String[]> p, CriteriaBuilder cb,
                                                             From<?, HoldingReproduction> hrRoot,
                                                             From<?, Reproduction> rRoot,
                                                             From<?, Holding> hRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression e = rRoot.get(Reproduction_.creationDate);

        if (containsSort) {
            String sort = p.get("sort")[0];
            if (sort.equals("customerName"))
                e = rRoot.get(Reproduction_.customerName);
            else if (sort.equals("customerEmail"))
                e = rRoot.get(Reproduction_.customerEmail);
            else if (sort.equals("status"))
                e = rRoot.get(Reproduction_.status);
            else if (sort.equals("printed"))
                e = hrRoot.get(HoldingReproduction_.printed);
            else if (sort.equals("signature"))
                e = hRoot.get(Holding_.signature);
            else if (sort.equals("holdingStatus"))
                e = hRoot.get(Holding_.status);
        }

        if (containsSortDir && p.get("sort_dir")[0].toLowerCase().equals("asc"))
            return cb.asc(e);
        return cb.desc(e);
    }
}
