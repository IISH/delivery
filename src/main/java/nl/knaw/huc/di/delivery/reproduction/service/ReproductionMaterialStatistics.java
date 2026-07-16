package nl.knaw.huc.di.delivery.reproduction.service;

import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.request.service.TupleRequestSearch;

import jakarta.persistence.criteria.*;
import java.util.Date;
import java.util.Map;

/**
 * Reproduction statistics helper class.
 */
public class ReproductionMaterialStatistics extends TupleRequestSearch<HoldingReproduction> {
    /**
     * Creates a new reproduction search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReproductionMaterialStatistics(CriteriaBuilder cb, Map<String, String[]> p) {
        super(HoldingReproduction.class, cb, p);
    }

    /**
     * Build the query.
     *
     * @param hrRoot The root entity.
     * @param cq     The query to build upon.
     */
    @Override
    protected void build(Root<HoldingReproduction> hrRoot, CriteriaQuery<?> cq) {
        // Join all required tables
        Join<HoldingReproduction, Reproduction> repRoot = hrRoot.join("reproduction");
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join("holding");
        Join<Holding, Record> rRoot = hRoot.join("record");
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join("externalInfo");

        // Count the materials
        Expression<Date> reproductionDate = repRoot.get("date");
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get("materialType");
        Expression<Long> numberOfRequests = cb.count(materialType);

        Predicate datePredicate = getDatePredicate(reproductionDate, true);

        cq.multiselect(materialType.alias("material"), numberOfRequests.alias("noRequests"));
        cq.where(datePredicate);
        cq.groupBy(eriRoot.get("materialType"));
        cq.orderBy(cb.desc(numberOfRequests));
    }
}
