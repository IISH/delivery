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
        Join<HoldingReservation, Reservation> resRoot = hrRoot.join("reservation");
        Join<HoldingReservation, Holding> hRoot = hrRoot.join("holding");
        Join<Holding, Record> rRoot = hRoot.join("record");
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join("externalInfo");

        Expression<Date> reservationDate = resRoot.get("date");
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get("materialType");
        Expression<Long> numberOfRequests = cb.count(materialType);

        Predicate datePredicate = getDatePredicate(reservationDate, true);
        Predicate materialPredicate = getMaterialPredicate(materialType);

        cq.multiselect(materialType.alias("material"), numberOfRequests.alias("noRequests"));
        cq.where((materialPredicate != null) ? cb.and(datePredicate, materialPredicate) : datePredicate);
        cq.groupBy(eriRoot.get("materialType"));
        cq.orderBy(cb.desc(numberOfRequests));
    }
}
