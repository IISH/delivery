package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.request.service.TupleRequestSearch;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation_;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.socialhistoryservices.delivery.reservation.entity.Reservation_;

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
        Join<HoldingReservation, Reservation> resRoot = hrRoot.join(HoldingReservation_.reservation);
        Join<HoldingReservation, Holding> hRoot = hrRoot.join(HoldingReservation_.holding);
        Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join(Record_.externalInfo);
        Join<Record, Record> prRoot = rRoot.join(Record_.parent, JoinType.LEFT);
        Join<Record, Holding> phRoot = prRoot.join(Record_.holdings, JoinType.LEFT);
        Join<Record, ExternalRecordInfo> periRoot = prRoot.join(Record_.externalInfo, JoinType.LEFT);

        Expression<Date> reservationDate = resRoot.get(Reservation_.date);
        Expression<String> parentSignature = phRoot.get(Holding_.signature);
        Expression<String> signature = hRoot.get(Holding_.signature);
        Expression<String> parentTitle = periRoot.get(ExternalRecordInfo_.title);
        Expression<String> title = eriRoot.get(ExternalRecordInfo_.title);
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get(ExternalRecordInfo_.materialType);
        Expression<Long> numberOfRequests = cb.count(signature);

        Predicate datePredicate = getDatePredicate(reservationDate, true);
        Predicate materialPredicate = getMaterialPredicate(materialType);

        cq.multiselect(
                parentSignature.alias("parentSignature"),
                signature.alias("signature"),
                parentTitle.alias("parentTitle"),
                title.alias("title"),
                numberOfRequests.alias("numberOfRequests")
        );
        cq.where((materialPredicate != null) ? cb.and(datePredicate, materialPredicate) : datePredicate);
        cq.groupBy(parentSignature, signature, parentTitle, title);
        cq.orderBy(cb.desc(numberOfRequests), cb.asc(parentSignature), cb.asc(signature));
    }
}
