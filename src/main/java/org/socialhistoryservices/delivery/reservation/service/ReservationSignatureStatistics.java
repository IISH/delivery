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
 * Reservation signature statistics helper class.
 */
public class ReservationSignatureStatistics extends TupleRequestSearch<HoldingReservation> {

    /**
     * Creates a new reservation signature statistics search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReservationSignatureStatistics(CriteriaBuilder cb, Map<String, String[]> p) {
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
        Join<Record, Record> prRoot = rRoot.join(Record_.parent, JoinType.LEFT);
        Join<Record, Holding> phRoot = prRoot.join(Record_.holdings, JoinType.LEFT);
        Join<Record, ExternalRecordInfo> periRoot = prRoot.join(Record_.externalInfo, JoinType.LEFT);

        Expression<Date> reservationDate = resRoot.get(Reservation_.date);
        Expression<Boolean> fromExpr = cb.greaterThanOrEqualTo(reservationDate, from);
        Expression<Boolean> toExpr = cb.lessThanOrEqualTo(reservationDate, to);

        Predicate where = cb.and(fromExpr, toExpr);

        if (p.containsKey("material")) {
            String material = p.get("material")[0].trim().toUpperCase();
            // Tolerant to empty material type to ensure the filter works
            if (!material.equals("")) {
                try {
                    Expression<Boolean> materialType = cb.equal(
                            eriRoot.get(ExternalRecordInfo_.materialType),
                            ExternalRecordInfo.MaterialType.valueOf(material)
                    );
                    where = cb.and(where, materialType);
                }
                catch (IllegalArgumentException ex) {
                    throw new InvalidRequestException("No such material: " + material);
                }
            }
        }

        Expression<String> parentSignature = phRoot.get(Holding_.signature);
        Expression<String> signature = hRoot.get(Holding_.signature);
        Expression<String> parentTitle = periRoot.get(ExternalRecordInfo_.title);
        Expression<String> title = eriRoot.get(ExternalRecordInfo_.title);
        Expression<Long> numberOfRequests = cb.count(signature);

        cq.multiselect(
                parentSignature.alias("parentSignature"),
                signature.alias("signature"),
                parentTitle.alias("parentTitle"),
                title.alias("title"),
                numberOfRequests.alias("numberOfRequests")
        );
        cq.where(where);
        cq.groupBy(parentSignature, signature, parentTitle, title);
        cq.orderBy(cb.desc(numberOfRequests), cb.asc(parentSignature), cb.asc(signature));
    }
}
