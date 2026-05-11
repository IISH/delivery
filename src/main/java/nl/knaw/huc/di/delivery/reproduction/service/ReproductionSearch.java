package nl.knaw.huc.di.delivery.reproduction.service;

import nl.knaw.huc.di.delivery.request.service.ListRequestSearch;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;

import jakarta.persistence.criteria.*;
import java.util.Map;

/**
 * Reproduction search helper class, with support for paging.
 */
public class ReproductionSearch extends ListRequestSearch<HoldingReproduction> {
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
     * @param hrRoot  The root entity.
     * @param cq      The query to build upon.
     * @param isCount Whether the query is a count or not.
     */
    @Override
    protected void build(Root<HoldingReproduction> hrRoot, CriteriaQuery<?> cq, boolean isCount) {
        Join<HoldingReproduction, Reproduction> rRoot = hrRoot.join("reproduction");

        // Expression to be the where clause of the query
        Expression<Boolean> where = null;

        // Filters
        where = addDateFilter(rRoot, where);
        where = addNameFilter(rRoot, where);
        where = addEmailFilter(rRoot, where);
        where = addStatusFilter(rRoot, where);
        where = addPrintedFilter(hrRoot, where);
        where = addSearchFilter(hrRoot, rRoot, where);

        // Set the where clause
        if (where != null)
            cq.where(where);

        Join<HoldingReproduction, Holding> hRoot = hrRoot.join("holding");

        if (!isCount)
            cq.orderBy(parseSortFilter(hrRoot, rRoot, hRoot));
    }

    /**
     * Add the date/from_date/to_date filter to the where clause, if present.
     *
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addDateFilter(Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        Predicate datePredicate = getDatePredicate(rRoot.get("date"), false);
        if (datePredicate != null)
            where = (where != null) ? cb.and(where, datePredicate) : datePredicate;
        return where;
    }

    /**
     * Add the name filter to the where clause, if present.
     *
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addNameFilter(Join<HoldingReproduction, Reproduction> rRoot,
                                              Expression<Boolean> where) {
        if (p.containsKey("customerName")) {
            Expression<Boolean> exName = cb.like(rRoot.get("customerName"),
                    "%" + p.get("customerName")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exName) : exName;
        }
        return where;
    }

    /**
     * Add the email filter to the where clause, if present.
     *
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addEmailFilter(Join<HoldingReproduction, Reproduction> rRoot,
                                               Expression<Boolean> where) {
        if (p.containsKey("customerEmail")) {
            Expression<Boolean> exEmail = cb.like(rRoot.get("customerEmail"),
                    "%" + p.get("customerEmail")[0].trim() + "%");
            where = (where != null) ? cb.and(where, exEmail) : exEmail;
        }
        return where;
    }

    /**
     * Add the status filter to the where clause, if present.
     *
     * @param rRoot The reproduction root.
     * @param where The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addStatusFilter(Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("status")) {
            String status = p.get("status")[0].trim().toUpperCase();
            if (!status.isEmpty()) { // Tolerant to empty status
                try {
                    Expression<Boolean> exStatus = cb.equal(rRoot.get("status"),
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
     * @param hrRoot The holding reproduction root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addPrintedFilter(Root<HoldingReproduction> hrRoot, Expression<Boolean> where) {
        if (p.containsKey("printed")) {
            String printed = p.get("printed")[0].trim().toLowerCase();
            if (printed.isEmpty()) {
                return where;
            }

            Expression<Boolean> exPrinted = cb.equal(hrRoot.get("printed"),
                    Boolean.parseBoolean(p.get("printed")[0]));
            where = (where != null) ? cb.and(where, exPrinted) : exPrinted;
        }
        return where;
    }

    /**
     * Add the search filter to the where clause, if present.
     *
     * @param hrRoot The holding reproduction root.
     * @param rRoot  The reproduction root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addSearchFilter(Root<HoldingReproduction> hrRoot,
                                                Join<HoldingReproduction, Reproduction> rRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("search") && !p.get("search")[0].trim().isEmpty()) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<HoldingReproduction, Holding> hRoot = hrRoot.join("holding");
            Join<Holding, Record> recRoot = hRoot.join("record");
            Join<Record, ExternalRecordInfo> eRoot = recRoot.join("externalInfo");
            Join<Record, Record> prRoot = recRoot.join("parent", JoinType.LEFT);
            Join<Record, Holding> phRoot = prRoot.join("holdings", JoinType.LEFT);

            Expression<Boolean> exSearch = cb.or(
                    cb.like(cb.lower(eRoot.get("title")), "%" + search + "%"),
                    cb.like(cb.lower(rRoot.get("customerName")), "%" + search + "%"),
                    cb.like(cb.lower(rRoot.get("customerEmail")), "%" + search + "%"),
                    cb.like(cb.lower(hRoot.get("signature")), "%" + search + "%"),
                    cb.like(cb.lower(phRoot.get("signature")), "%" + search + "%")
            );

            where = (where != null) ? cb.and(where, exSearch) : exSearch;
        }
        return where;
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query.
     *
     * @param hrRoot The root of the reproduction holding used to construct the Order.
     * @param rRoot  The root of the reproduction used to construct the Order.
     * @param hRoot  The root of the holding used to construct the Order.
     * @return The order the query should be in (asc/desc) sorted on provided column. Defaults to asc on the PK column.
     */
    private jakarta.persistence.criteria.Order parseSortFilter(From<?, HoldingReproduction> hrRoot,
                                                             From<?, Reproduction> rRoot, From<?, Holding> hRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression<?> e = rRoot.get("creationDate");

        if (containsSort) {
            String sort = p.get("sort")[0];
            e = switch (sort) {
                case "customerName" -> rRoot.get("customerName");
                case "customerEmail" -> rRoot.get("customerEmail");
                case "status" -> rRoot.get("status");
                case "printed" -> hrRoot.get("printed");
                case "signature" -> hRoot.get("signature");
                case "holdingStatus" -> hRoot.get("status");
                default -> e;
            };
        }

        if (containsSortDir && p.get("sort_dir")[0].equalsIgnoreCase("asc"))
            return cb.asc(e);
        return cb.desc(e);
    }
}
