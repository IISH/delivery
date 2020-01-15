package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.request.service.ListRequestSearch;
import org.socialhistoryservices.delivery.util.InvalidRequestException;
import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation_;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation_;

import javax.persistence.criteria.*;
import java.util.ArrayList;
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
        Join<HoldingReservation, Reservation> resRoot = hrRoot.join(HoldingReservation_.reservation);

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

        Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);

        if (!isCount) {
            cq.orderBy(
                    parseSortFilter(hrRoot, resRoot, hRoot)
            );
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
    private ArrayList<Order> parseSortFilter(From<?, HoldingReservation> hrRoot, From<?, Reservation> resRoot, From<?, Holding> hRoot) {
        boolean containsSort = p.containsKey("sort");
        boolean containsSortDir = p.containsKey("sort_dir");
        Expression e = resRoot.get(Reservation_.date);

        ArrayList<Order> sortOrder = new ArrayList<>();
        sortOrder.add(cb.desc(resRoot.get(Reservation_.date)));
        sortOrder.add(cb.asc(resRoot.get(Reservation_.visitorName)));
        sortOrder.add(cb.asc(hRoot.get(Holding_.signature)));

        if (containsSort) {
            String sort = p.get("sort")[0];
            switch (sort) {
                case "visitorName":
                    e = resRoot.get(Reservation_.visitorName);
                    break;
                case "status":
                    e = resRoot.get(Reservation_.status);
                    break;
                case "printed":
                    e = hrRoot.get(HoldingReservation_.printed);
                    break;
                case "signature":
                    e = hRoot.get(Holding_.signature);
                    break;
                case "holdingStatus":
                    e = hRoot.get(Holding_.status);
                    break;
            }
        }

        if (containsSortDir && p.get("sort_dir")[0].toLowerCase().equals("asc")) {
            sortOrder.add(cb.asc(e));
            return sortOrder;
        }

        sortOrder.add(cb.desc(e));
        return sortOrder;
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
        if (p.containsKey("search") && !p.get("search")[0].trim().equals("")) {
            String search = p.get("search")[0].trim().toLowerCase();

            Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);
            Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
            Join<Record, ExternalRecordInfo> eRoot = rRoot.join(Record_.externalInfo);
            Join<Record, Record> prRoot = rRoot.join(Record_.parent, JoinType.LEFT);
            Join<Record, Holding> phRoot = prRoot.join(Record_.holdings, JoinType.LEFT);
            Expression<Boolean> exSearch = cb.or(
                    cb.like(cb.lower(eRoot.get(ExternalRecordInfo_.title)), "%" + search + "%"),
                    cb.like(cb.lower(resRoot.get(Reservation_
                            .visitorName)), "%" + search + "%"),
                    cb.like(cb.lower(resRoot.get(Reservation_
                            .visitorEmail)), "%" + search + "%"),
                    cb.like(cb.lower(hRoot.get(Holding_.signature)), "%" + search + "%"),
                    cb.like(cb.lower(phRoot.<String>get(Holding_.signature)), "%" + search + "%")
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
                    hrRoot.get(HoldingReservation_.printed),
                    Boolean.parseBoolean(p.get("printed")[0]));
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
            // reservation_get_list.html.ftl works
            if (!status.equals("")) {
                try {
                    Expression<Boolean> exStatus = cb.equal(
                            resRoot.get(Reservation_.status), Reservation.Status.valueOf(status));
                    where = where != null ? cb.and(where, exStatus) : exStatus;
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such status: " +
                            status);
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
            Expression<Boolean> exEmail = cb.like(
                    resRoot.get(Reservation_.visitorEmail),
                    "%" + p.get("visitorEmail")[0].trim() + "%");
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
            Expression<Boolean> exName = cb.like(resRoot.get(Reservation_.visitorName),
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
        Predicate datePredicate = getDatePredicate(resRoot.get(Reservation_.date), false);
        if (datePredicate != null) {
            where = (where != null) ? cb.and(where, datePredicate) : datePredicate;
        }
        return where;
    }
}
