package org.socialhistoryservices.delivery.reproduction.entity;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.reservation.entity.HoldingReservation;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reproduction object representing a reproduction that can be made on a set of records.
 */
@Entity
@Table(name = "holding_reproductions", indexes = {
        @Index(columnList = "completed", name = "holding_reproductions_completed_idx"),
        @Index(columnList = "reproduction_id", name = "holding_reproductions_reproduction_fk"),
        @Index(columnList = "holding_id", name = "holding_reproductions_holding_fk")})
public class HoldingReproduction extends HoldingRequest {
    /**
     * The HoldingReproduction's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the HoldingReproduction's id.
     *
     * @return the HoldingReproduction's id.
     */
    @Override
    public int getId() {
        return id;
    }

    @Digits(integer = 5, fraction = 2)
    @Column(name = "price")
    private BigDecimal price;

    /**
     * Get the price.
     *
     * @return the price.
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Returns the complete price.
     * In case pages are known, the price per page is multiplied with the number of pages.
     *
     * @return the complete price.
     */
    public BigDecimal getCompletePrice() {
        return getPrice().multiply(new BigDecimal(this.getNumberOfPages()));
    }

    /**
     * Returns the complete price, including the discount.
     *
     * @return the complete price, including the discount.
     */
    public BigDecimal getCompletePriceWithDiscount() {
        BigDecimal price = getCompletePrice().subtract(getDiscount());

        // We cannot have a negative price
        if (price.compareTo(BigDecimal.ZERO) < 0)
            price = BigDecimal.ZERO;

        return price;
    }

    /**
     * Get the complete price without the BTW and discount
     *
     * @return the complete price without the BTW and discount
     */
    public BigDecimal getCompletePriceWithoutTax() {
        double percentage = (double) this.btwPercentage / 100 + 1;
        BigDecimal percentageDecimal = new BigDecimal(percentage);
        return this.getCompletePriceWithDiscount().divide(percentageDecimal, 2, RoundingMode.HALF_UP);
    }

    /**
     * Set the price.
     *
     * @param price the price.
     */
    public void setPrice(BigDecimal price) {
        if (price != null)
            price = price.setScale(2, RoundingMode.HALF_UP);
        this.price = price;
    }

    @NotNull
    @Min(1)
    @Column(name = "numberofpages", nullable = false)
    private int numberOfPages = 1;

    /**
     * Get the number of pages (in case of books and brochures).
     *
     * @return the number of pages (in case of books and brochures).
     */
    public int getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Set the number of pages (in case of books and brochures).
     *
     * @param numberOfPages the number of pages (in case of books and brochures).
     */
    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    @Min(0)
    @Column(name = "deliverytime")
    private Integer deliveryTime;

    /**
     * Get the delivery time in days.
     *
     * @return the delivery time in days.
     */
    public Integer getDeliveryTime() {
        return deliveryTime;
    }

    /**
     * Set the delivery time in days.
     *
     * @param deliveryTime the delivery time in days.
     */
    public void setDeliveryTime(Integer deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    @Digits(integer = 5, fraction = 2)
    @Column(name = "discount")
    private BigDecimal discount;

    /**
     * Get the computated discount for this item.
     *
     * @return the computated discount for this item.
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * Set the computated discount for this item.
     *
     * @param discount the computated discount for this item.
     */
    public void setDiscount(BigDecimal discount) {
        if (discount != null)
            discount = discount.setScale(2, RoundingMode.HALF_UP);
        this.discount = discount;
    }

    @Digits(integer = 5, fraction = 2)
    @Column(name = "btw_price")
    private BigDecimal btwPrice;

    /**
     * Get the price for BTW.
     *
     * @return the price for BTW.
     */
    public BigDecimal getBtwPrice() {
        return btwPrice;
    }

    /**
     * Set the price for BTW.
     *
     * @param btwPrice the price for BTW.
     */
    public void setBtwPrice(BigDecimal btwPrice) {
        if (btwPrice != null)
            btwPrice = btwPrice.setScale(2, RoundingMode.HALF_UP);
        this.btwPrice = btwPrice;
    }

    @Min(0)
    @Max(100)
    @Column(name = "btw_percentage")
    private Integer btwPercentage;

    /**
     * Get the BTW percentage.
     *
     * @return the BTW percentage.
     */
    public Integer getBtwPercentage() {
        return btwPercentage;
    }

    /**
     * Set the BTW percentage.
     *
     * @param btwPercentage the BTW percentage.
     */
    public void setBtwPercentage(Integer btwPercentage) {
        this.btwPercentage = btwPercentage;
    }

    /**
     * The comment on a specific holding in a reproduction.
     */
    @Size(max = 255)
    @Column(name = "comment")
    private String comment;

    /**
     * Get the comment on a specific holding in a reproduction.
     *
     * @return The comment.
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Set the comment on a specific holding in a reproduction.
     *
     * @param val The value to set the comment to.
     */
    @Override
    public void setComment(String val) {
        comment = val;
    }

    /**
     * Whether the print for this holding has been printed or not.
     */
    @Column(name = "printed")
    private boolean printed = false;

    /**
     * Set whether the HoldingReproduction was printed or not.
     *
     * @param b True to consider the HoldingReproduction to be printed at least once, false otherwise.
     */
    @Override
    public void setPrinted(boolean b) {
        printed = b;
    }

    /**
     * Check if the HoldingReproduction (is considered) to be printed at least once.
     *
     * @return True if the HoldingReproduction was printed at least once, false otherwise.
     */
    @Override
    public boolean isPrinted() {
        return printed;
    }

    /**
     * Is the holding completed for this reproduction?
     */
    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    /**
     * Is the holding completed for this reproduction?
     *
     * @return Whether this holding is completed for this reproduction.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set the holding completed for this reproduction?
     *
     * @param completed Whether the holding has been completed for this reproduction.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * Is there already a reproduction in the SOR?
     */
    @NotNull
    @Column(name = "insor", nullable = false)
    private boolean inSor;

    /**
     * Get whether there already is a reproduction in the SOR.
     *
     * @return Is there already a reproduction in the SOR?
     */
    public boolean isInSor() {
        return inSor;
    }

    /**
     * Set whether there already is a reproduction in the SOR.
     *
     * @param inSor Is there already a reproduction in the SOR?
     */
    public void setInSor(boolean inSor) {
        this.inSor = inSor;
        this.completed = inSor; // If already in the SOR, no need for it to go to repro
    }

    /**
     * The kind of custom reproduction the customer requires.
     */
    @Column(name = "customreproductioncustomer", columnDefinition = "TEXT")
    private String customReproductionCustomer;

    /**
     * Get the kind of custom reproduction the customer requires.
     *
     * @return the kind of custom reproduction the customer requires.
     */
    public String getCustomReproductionCustomer() {
        return customReproductionCustomer;
    }

    /**
     * Set the kind of custom reproduction the customer requires.
     *
     * @param customReproductionCustomer the kind of custom reproduction the customer requires.
     */
    public void setCustomReproductionCustomer(String customReproductionCustomer) {
        this.customReproductionCustomer = customReproductionCustomer;
    }

    /**
     * The kind of custom reproduction reply.
     */
    @Column(name = "customreproductionreply", columnDefinition = "TEXT")
    private String customReproductionReply;

    /**
     * Get the kind of custom reproduction reply.
     *
     * @return the kind of custom reproduction reply.
     */
    public String getCustomReproductionReply() {
        return customReproductionReply;
    }

    /**
     * Set the kind of custom reproduction reply.
     *
     * @param customReproductionReply the kind of custom reproduction reply.
     */
    public void setCustomReproductionReply(String customReproductionReply) {
        this.customReproductionReply = customReproductionReply;
    }

    /**
     * The HoldingReproduction's reproduction.
     */
    @ManyToOne
    @JoinColumn(name = "reproduction_id")
    private Reproduction reproduction;

    /**
     * Get the HoldingReproduction's reproduction.
     *
     * @return the HoldingReproduction's reproduction.
     */
    public Reproduction getReproduction() {
        return reproduction;
    }

    /**
     * Set the HoldingReproduction's reproduction.
     *
     * @param reproduction the HoldingReproduction's reproduction.
     */
    public void setReproduction(Reproduction reproduction) {
        this.reproduction = reproduction;
    }

    /**
     * Get the HoldingRequest's request.
     *
     * @return the HoldingRequest's request.
     */
    @Override
    public Request getRequest() {
        return getReproduction();
    }

    /**
     * Set the HoldingRequest's request.
     *
     * @param request
     */
    @Override
    public void setRequest(Request request) {
        setReproduction((Reproduction) request);
    }

    /**
     * The HoldingReproduction's holding.
     */
    @ManyToOne
    @JoinColumn(name = "holding_id")
    private Holding holding;

    /**
     * Get the HoldingReproduction's holding.
     *
     * @return the HoldingReproduction's holding.
     */
    @Override
    public Holding getHolding() {
        return holding;
    }

    /**
     * Set the HoldingReproduction's holding.
     *
     * @param h the HoldingReproduction's holding.
     */
    @Override
    public void setHolding(Holding h) {
        this.holding = h;
    }

    /**
     * The HoldingReproduction's standard option (if chosen).
     */
    @ManyToOne
    @JoinColumn(name = "reproduction_standard_option_id")
    private ReproductionStandardOption standardOption;

    public ReproductionStandardOption getStandardOption() {
        return standardOption;
    }

    public void setStandardOption(ReproductionStandardOption standardOption) {
        this.standardOption = standardOption;
    }

    /**
     * Whether the price and delivery time is determined and thus has all order details.
     *
     * @return Whether this holding contains all order details.
     */
    public boolean hasOrderDetails() {
        return ((getPrice() != null) && (getDeliveryTime() != null));
    }

    /**
     * Merge this HoldingRequest with another HoldingRequest.
     *
     * @param other Another HoldingRequest.
     */
    public void mergeWith(HoldingRequest other) {
        super.mergeWith(other);
        if (other instanceof HoldingReproduction) {
            final HoldingReproduction otherHr = (HoldingReproduction) other;

            setBtwPrice(otherHr.getBtwPrice());
            setBtwPercentage(otherHr.getBtwPercentage());
            setDiscount(otherHr.getDiscount());
            setInSor(otherHr.isInSor());

            setStandardOption(otherHr.getStandardOption());
            setPrice(otherHr.getPrice());
            setNumberOfPages(otherHr.getNumberOfPages());
            setDeliveryTime(otherHr.getDeliveryTime());

            setCustomReproductionCustomer(otherHr.getCustomReproductionCustomer());
            setCustomReproductionReply(otherHr.getCustomReproductionReply());
        } else {
            this.customReproductionCustomer = "NA";
        }
    }
}
