package org.socialhistoryservices.delivery.reproduction.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility methods for dealing with currencies using BigDecimal.
 */
public class BigDecimalUtils {
    /**
     * Returns the percentage of a given amount.
     *
     * @param amount     The amount.
     * @param percentage The percentage to return.
     * @return The percentage of a given amount.
     */
    public static BigDecimal getPercentageOfAmount(BigDecimal amount, int percentage) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(percentage))
                .divide(new BigDecimal(100), RoundingMode.HALF_UP);
    }

    /**
     * Returns the BTW amount of a given amount.
     *
     * @param amount     The total amount.
     * @param percentage The BTW percentage.
     * @return The BTW amount.
     */
    public static BigDecimal getBtwAmount(BigDecimal amount, int percentage) {
        BigDecimal btwPercentage = new BigDecimal(percentage);
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(btwPercentage)
                .divide(btwPercentage.add(new BigDecimal(100)), RoundingMode.HALF_UP);
    }
}
