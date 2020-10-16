package org.socialhistoryservices.delivery.reproduction.service;

import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction;
import org.socialhistoryservices.delivery.reproduction.entity.HoldingReproduction_;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction;
import org.socialhistoryservices.delivery.reproduction.entity.Reproduction_;
import org.socialhistoryservices.delivery.request.service.TupleRequestSearch;

import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Reproduction statistics helper class.
 */
public class ReproductionPaymentStatistics extends TupleRequestSearch<HoldingReproduction> {
    /**
     * Creates a new reproduction search helper.
     *
     * @param cb The criteria builder.
     * @param p  The parameters from the user.
     */
    public ReproductionPaymentStatistics(CriteriaBuilder cb, Map<String, String[]> p) {
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
        Join<HoldingReproduction, Reproduction> repRoot = hrRoot.join(HoldingReproduction_.reproduction);

        // Within the selected date range
        Expression<Date> reproductionDate = repRoot.get(Reproduction_.datePaymentAccepted);
        Predicate datePredicate = getDatePredicate(reproductionDate, true);

        // And only active or completed reproductions
        Expression<Reproduction.Status> status = repRoot.get(Reproduction_.status);
        Expression<Boolean> statusExpr = cb.in(status)
                .value(Reproduction.Status.ACTIVE)
                .value(Reproduction.Status.COMPLETED)
                .value(Reproduction.Status.DELIVERED);

        // Count the amounts
        Expression<BigDecimal> amount = hrRoot.get(HoldingReproduction_.price);
        Expression<Integer> numberOfPages = hrRoot.get(HoldingReproduction_.numberOfPages);
        Expression<BigDecimal> discount = hrRoot.get(HoldingReproduction_.discount);
        Expression<BigDecimal> btwPrice = hrRoot.get(HoldingReproduction_.btwPrice);
        Expression<Integer> btwPercentage = hrRoot.get(HoldingReproduction_.btwPercentage);

        Expression<Number> totalAmount = cb.prod(amount, numberOfPages);

        Expression<Long> totalItems = cb.count(hrRoot);
        Expression<Number> sumTotalAmount = cb.sum(totalAmount);
        Expression<BigDecimal> sumDiscount = cb.sum(discount);
        Expression<BigDecimal> sumBtwPrice = cb.sum(btwPrice);

        cq.multiselect(
                totalItems.alias("totalItems"),
                btwPercentage.alias("btwPercentage"),
                sumTotalAmount.alias("sumTotalAmount"),
                sumDiscount.alias("sumDiscount"),
                sumBtwPrice.alias("sumBtwPrice")
        );
        cq.where(cb.and(statusExpr, datePredicate));
        cq.groupBy(btwPercentage);
    }
}
