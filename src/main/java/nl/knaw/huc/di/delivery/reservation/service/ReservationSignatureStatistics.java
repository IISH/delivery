package nl.knaw.huc.di.delivery.reservation.service;

import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.request.service.TupleRequestSearch;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import jakarta.persistence.criteria.*;
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
        Join<HoldingReservation, Reservation> resRoot = hrRoot.join("reservation");
        Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");
        Join<Holding, Record> rRoot = hRoot.join("record");
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join("externalInfo");
        Join<Record, Record> prRoot = rRoot.join("parent", JoinType.LEFT);
        Join<Record, Holding> phRoot = prRoot.join("holdings", JoinType.LEFT);
        Join<Record, ExternalRecordInfo> periRoot = prRoot.join("externalInfo", JoinType.LEFT);

        Expression<Date> reservationDate = resRoot.get("date");
        Expression<String> parentSignature = phRoot.get("signature");
        Expression<String> signature = hRoot.get("signature");
        Expression<String> parentTitle = periRoot.get("title");
        Expression<String> title = eriRoot.get("title");
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get("materialType");
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
