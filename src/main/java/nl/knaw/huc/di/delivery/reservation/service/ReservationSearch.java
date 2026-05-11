package nl.knaw.huc.di.delivery.reservation.service;

import nl.knaw.huc.di.delivery.request.service.ListRequestSearch;
import nl.knaw.huc.di.delivery.config.InvalidRequestException;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import jakarta.persistence.criteria.*;
import java.util.Map;

/**
 * Reservation search helper class, with support for paging.
 */
public class ReservationSearch extends ListRequestSearch<HoldingReservation> {
    /**
     * Creates a new reservation search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReservationSearch(CriteriaBuilder cb, Map<String, String[]> p) {
        super(HoldingReservation.class, cb, p);
    }

    /**
     * Build the query.
     *
     * @param hrRoot  The root entity.
     * @param cq      The query to build upon.
     * @param isCount Whether the query is a count or not.
     */
    @Override
    protected void build(Root<HoldingReservation> hrRoot, CriteriaQuery<?> cq, boolean isCount) {
        Join<HoldingReservation, Reservation> resRoot = hrRoot.join("reservation");

        // Expression to be the where clause of the query
        Expression<Boolean> where = null;

        // Filters
        where = addDateFilter(resRoot, where);
        where = addNameFilter(resRoot, where);
        where = addEmailFilter(resRoot, where);
        where = addStatusFilter(resRoot, where);
        where = addPrintedFilter(hrRoot, where);
        where = addSearchFilter(hrRoot, resRoot, where);

        // Set the where clause
        if (where != null) {
            cq.where(where);
        }

        Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");

        if (!isCount) {
            cq.orderBy(parseSortFilter(hrRoot, resRoot, hRoot));
        }
    }

    /**
     * Parse the sort and sort_dir filters into an Order to be used in a query.
     *
     * @param hrRoot  The root of the holding reservation used to construct the Order.
     * @param resRoot The root of the reservation used to construct the Order.
     * @param hRoot   The root of the holding used to construct the Order.
     * @return The order the query should be in (asc/desc) sorted on provided
     * column. Defaults to asc on the PK column.
     */
    private Order parseSortFilter(From<?, HoldingReservation> hrRoot, From<?, Reservation> resRoot,
                                  From<?, Holding> hRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");

        Expression<?> e = resRoot.get("date");
        if (containsSort) {
            String sort = p.get("sort")[0];
            e = switch (sort) {
                case "visitorName" -> resRoot.get("visitorName");
                case "status" -> resRoot.get("status");
                case "printed" -> hrRoot.get("printed");
                case "signature" -> hRoot.get("signature");
                case "holdingStatus" -> hRoot.get("status");
                default -> e;
            };
        }

        if (containsSortDir && p.get("sort_dir")[0].equalsIgnoreCase("asc")) {
            return cb.asc(e);
        }

        return cb.desc(e);
    }

    /**
     * Add the search filter to the where clause, if present.
     *
     * @param hrRoot  The holding reservation root.
     * @param resRoot The reservation root.
     * @param where   The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addSearchFilter(Root<HoldingReservation> hrRoot,
                                                Join<HoldingReservation, Reservation> resRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("search") && !p.get("search")[0].trim().isEmpty()) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");
            Join<Holding, Record> rRoot = hRoot.join("record");
            Join<Record, ExternalRecordInfo> eRoot = rRoot.join("externalInfo");
            Join<Record, Record> prRoot = rRoot.join("parent", JoinType.LEFT);
            Join<Record, Holding> phRoot = prRoot.join("holdings", JoinType.LEFT);
            Expression<Boolean> exSearch = cb.or(
                    cb.like(cb.lower(eRoot.get("title")), "%" + search + "%"),
                    cb.like(cb.lower(resRoot.get(
                            "visitorName")), "%" + search + "%"),
                    cb.like(cb.lower(resRoot.get(
                            "visitorEmail")), "%" + search + "%"),
                    cb.like(cb.lower(hRoot.get("signature")), "%" + search + "%"),
                    cb.like(cb.lower(phRoot.get("signature")), "%" + search + "%")
            );
            where = where != null ? cb.and(where, exSearch) : exSearch;
        }
        return where;
    }

    /**
     * Add the printed filter to the where clause, if present.
     *
     * @param hrRoot The holding reservation root.
     * @param where  The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addPrintedFilter(Root<HoldingReservation> hrRoot, Expression<Boolean> where) {
        if (p.containsKey("printed")) {
            String printed = p.get("printed")[0].trim().toLowerCase();
            if (printed.isEmpty()) {
                return where;
            }

            Expression<Boolean> exPrinted = cb.equal(
                    hrRoot.get("printed"),
                    Boolean.parseBoolean(p.get("printed")[0])
            );

            where = where != null ? cb.and(where, exPrinted) : exPrinted;
        }

        return where;
    }

    /**
     * Add the status filter to the where clause, if present.
     *
     * @param resRoot The reservation root.
     * @param where   The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addStatusFilter(Join<HoldingReservation, Reservation> resRoot,
                                                Expression<Boolean> where) {
        if (p.containsKey("status")) {
            String status = p.get("status")[0].trim().toUpperCase();
            // Tolerant to empty status to ensure the filter in
            // reservation_get_list.html.ftlh works
            if (!status.isEmpty()) {
                try {
                    Expression<Boolean> exStatus = cb.equal(
                            resRoot.get("status"), Reservation.Status.valueOf(status));
                    where = where != null ? cb.and(where, exStatus) : exStatus;
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such status: " + status);
                }
            }
        }
        return where;
    }

    /**
     * Add the email filter to the where clause, if present.
     *
     * @param resRoot The reservation root.
     * @param where   The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addEmailFilter(Join<HoldingReservation, Reservation> resRoot,
                                               Expression<Boolean> where) {
        if (p.containsKey("visitorEmail")) {
            Expression<Boolean> exEmail =
                    cb.like(resRoot.get("visitorEmail"), "%" + p.get("visitorEmail")[0].trim() + "%");
            where = where != null ? cb.and(where, exEmail) : exEmail;
        }
        return where;
    }

    /**
     * Add the name filter to the where clause, if present.
     *
     * @param resRoot The reservation root.
     * @param where   The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addNameFilter(Join<HoldingReservation, Reservation> resRoot,
                                              Expression<Boolean> where) {
        if (p.containsKey("visitorName")) {
            Expression<Boolean> exName = cb.like(resRoot.get("visitorName"),
                    "%" + p.get("visitorName")[0].trim() + "%");
            where = where != null ? cb.and(where, exName) : exName;
        }
        return where;
    }

    /**
     * Add the date/from_date/to_date filter to the where clause, if present.
     *
     * @param resRoot The reservation root.
     * @param where   The already present where clause or null if none present.
     * @return The (updated) where clause, or null if the filter did not exist.
     */
    private Expression<Boolean> addDateFilter(Join<HoldingReservation, Reservation> resRoot,
                                              Expression<Boolean> where) {
        Predicate datePredicate = getDatePredicate(resRoot.get("date"), false);
        if (datePredicate != null)
            where = (where != null) ? cb.and(where, datePredicate) : datePredicate;
        return where;
    }
}
