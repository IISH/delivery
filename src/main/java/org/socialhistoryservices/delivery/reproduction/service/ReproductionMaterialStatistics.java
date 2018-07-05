package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.record.entity.*;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction_;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction_;
import org.socialhistoryservices.delivery.request.service.TupleRequestSearch;

import javax.persistence.criteria.*;
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
        Date from = getFromDateFilter(p);
        from = (from != null) ? from : new Date();
        Date to = getToDateFilter(p);
        to = (to != null) ? to : new Date();

        // Join all required tables
        Join<HoldingReproduction, Reproduction> repRoot = hrRoot.join(HoldingReproduction_.reproduction);
        Join<HoldingReproduction, Holding> hRoot = hrRoot.join(HoldingReproduction_.holding);
        Join<Holding, Record> rRoot = hRoot.join(Holding_.record);
        Join<Record, ExternalRecordInfo> eriRoot = rRoot.join(Record_.externalInfo);

        // Within the selected date range
        Expression<Date> reproductionDate = repRoot.get(Reproduction_.date);
        Expression<Boolean> fromExpr = cb.greaterThanOrEqualTo(reproductionDate, from);
        Expression<Boolean> toExpr = cb.lessThanOrEqualTo(reproductionDate, to);

        // Count the materials
        Expression<ExternalRecordInfo.MaterialType> materialType =
                eriRoot.get(ExternalRecordInfo_.materialType);
        Expression<Long> numberOfRequests = cb.count(materialType);

        cq.multiselect(materialType.alias("material"), numberOfRequests.alias("noRequests"));
        cq.where(cb.and(fromExpr, toExpr));
        cq.groupBy(eriRoot.get(ExternalRecordInfo_.materialType));
        cq.orderBy(cb.desc(numberOfRequests));
    }
}
