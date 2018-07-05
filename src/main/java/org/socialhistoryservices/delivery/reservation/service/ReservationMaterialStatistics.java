package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.request.service.TupleRequestSearch;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation_;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation_;
import org.socialhistoryservices.delivery.util.InvalidRequestException;

import javax.persistence.criteria.*;
import java.util.Date;
import java.util.Map;

/**
 * Reservation material statistics helper class.
 */
public class ReservationMaterialStatistics extends TupleRequestSearch<HoldingReservation> {

    /**
     * Creates a new reservation search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReservationMaterialStatistics(CriteriaBuilder cb, Map<String, String[]> p) {
        super(HoldingReservation.class, cb, p);
    }

    /**
     * Build the query.
     *
     * @param hrRoot The root entity.
     * @param cq     The query to build upon.
     */
    @Override
    protected void build(Root<HoldingReservation> hrRoot, CriteriaQuery<?> cq) {
        Date from = getFromDateFilter(p);
        from = (from != null) ? from : new Date();
        Date to = getToDateFilter(p);
        to = (to != null) ? to : new Date();

        Join<HoldingReservation, Reservation> resRoot = hrRoot.join(HoldingReservation_.reservation);
        Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);
        Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join(Record_.externalInfo);

        Expression<Date> reservationDate = resRoot.get(Reservation_.date);
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get(ExternalRecordInfo_.materialType);
        Expression<Long> numberOfRequests = cb.count(materialType);

        Expression<Boolean> fromExpr = cb.greaterThanOrEqualTo(reservationDate, from);
        Expression<Boolean> toExpr = cb.lessThanOrEqualTo(reservationDate, to);

        Predicate where = cb.and(fromExpr, toExpr);

        if (p.containsKey("material")) {
            String material = p.get("material")[0].trim().toUpperCase();
            // Tolerant to empty material type to ensure the filter works
            if (!material.equals("")) {
                try {
                    Expression<Boolean> materialTypeMatches = cb.equal(
                            eriRoot.get(ExternalRecordInfo_.materialType),
                            ExternalRecordInfo.MaterialType.valueOf(material)
                    );
                    where = cb.and(where, materialTypeMatches);
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such material: " + material);
                }
            }
        }

        cq.multiselect(materialType.alias("material"), numberOfRequests.alias("noRequests"));
        cq.where(where);
        cq.groupBy(eriRoot.get(ExternalRecordInfo_.materialType));
        cq.orderBy(cb.desc(numberOfRequests));
    }
}
