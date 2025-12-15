package org.socialhistoryservices.delivery.reproduction.entity;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.socialhistoryservices.delivery.reservation.entity.Reservation;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Reproduction object representing a Reproduction that can be made on a set of records.
 */
@Entity
@Table(name = "reproductions")
@Configurable
public class Reproduction extends Request {

    /**
     * Status of the reproduction.
     */
    public enum Status {
        WAITING_FOR_ORDER_DETAILS,
        HAS_ORDER_DETAILS,
        CONFIRMED,
        ACTIVE,
        COMPLETED,
        DELIVERED,
        CANCELLED
    }

    /**
     * The Reproduction's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the Reproduction's id.
     *
     * @return the Reproduction's id.
     */
    public int getId() {
        return id;
    }

    /**
     * The customers name.
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "customername", nullable = false)
    private String customerName;

    /**
     * Get the customers name.
     *
     * @return the customers name.
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Set the customers name.
     *
     * @param name the customers name.
     */
    public void setCustomerName(String name) {
        this.customerName = name;
    }

    /**
     * Returns the name of the person making the request.
     *
     * @return The name of the person.
     */
    @Override
    public String getName() {
        return getCustomerName();
    }

    /**
     * The customers email.
     */
    @NotBlank
    @Size(max = 255)
    @Email
    @Column(name = "customeremail", nullable = false)
    private String customerEmail;

    /**
     * Get the customers email.
     *
     * @return the customers email.
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * Set the customers email.
     *
     * @param email the customers email.
     */
    public void setCustomerEmail(String email) {
        this.customerEmail = email;
    }

    /**
     * Returns the email address of the person making the request.
     *
     * @return The email address of the person.
     */
    @Override
    public String getEmail() {
        return getCustomerEmail();
    }

    /**
     * The Reproduction's date.
     */
    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "date", nullable = false)
    private Date date;

    /**
     * Get the Reproduction's date.
     *
     * @return the Reproduction's date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the Reproduction's date.
     *
     * @param date the Reproduction's date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Whether the offer for this reproduction was ready immediatly after creation.
     */
    @Column(name = "offer_ready_immediatly")
    private boolean offerReadyImmediatly;

    /**
     * Get whether the offer for this reproduction was ready immediatly after creation.
     *
     * @return Whether the offer for this reproduction was ready immediatly after creation.
     */
    public boolean isOfferReadyImmediatly() {
        return offerReadyImmediatly;
    }

    /**
     * Set whether the offer for this reproduction was ready immediatly after creation.
     *
     * @param offerReadyImmediatly Whether the offer for this reproduction was ready immediatly after creation.
     */
    public void setOfferReadyImmediatly(boolean offerReadyImmediatly) {
        this.offerReadyImmediatly = offerReadyImmediatly;
    }

    /**
     * Whether the reminder mail for this reproduction has been sent.
     */
    @Column(name = "offer_mail_reminder_sent")
    private boolean offerMailReminderSent;

    /**
     * Get whether the reminder mail for this reproduction has been sent.
     *
     * @return Whether the reminder mail for this reproduction has been sent.
     */
    public boolean isOfferMailReminderSent() {
        return offerMailReminderSent;
    }

    /**
     * Set whether the reminder mail for this reproduction has been sent.
     *
     * @param offerMailReminderSent Whether the reminder mail for this reproduction has been sent.
     */
    public void setOfferMailReminderSent(boolean offerMailReminderSent) {
        this.offerMailReminderSent = offerMailReminderSent;
    }

    /**
     * The Reproduction's date when the order details were known.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "date_has_order_details")
    private Date dateHasOrderDetails;

    /**
     * Returns the Reproduction's date when the order details were known.
     *
     * @return The Reproduction's date when the order details were known.
     */
    public Date getDateHasOrderDetails() {
        return dateHasOrderDetails;
    }

    /**
     * Sets the Reproduction's date when the order details were known.
     *
     * @param dateHasOrderDetails The Reproduction's date when the order details were known.
     */
    public void setDateHasOrderDetails(Date dateHasOrderDetails) {
        this.dateHasOrderDetails = dateHasOrderDetails;
    }

    /**
     * The Reproduction's date when the payment was accepted.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "date_payment_accepted")
    private Date datePaymentAccepted;

    /**
     * Returns the Reproduction's date when the payment was accepted.
     *
     * @return The Reproduction's date when the payment was accepted.
     */
    public Date getDatePaymentAccepted() {
        return datePaymentAccepted;
    }

    /**
     * Sets the Reproduction's date when the payment was accepted.
     *
     * @param datePaymentAccepted The Reproduction's date when the payment was accepted.
     */
    public void setDatePaymentAccepted(Date datePaymentAccepted) {
        this.datePaymentAccepted = datePaymentAccepted;
    }

    /**
     * The Reproduction's creation date.
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    /**
     * Get the Reproduction's creation date.
     *
     * @return the Reproduction's creation date.
     */
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the Reproduction's date.
     *
     * @param creationDate the Reproduction's date.
     */
    @Override
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * The Reproduction's status.
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    /**
     * Get the Reproduction's status.
     *
     * @return the Reproduction's status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set the Reproduction's status.
     *
     * @param status the Reproduction's status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * The adminstration costs specified for this reproduction.
     */
    @NotNull
    @Min(0)
    @Digits(integer = 5, fraction = 2)
    @Column(name = "adminstrationcosts", nullable = false)
    private BigDecimal adminstrationCosts;

    /**
     * Get the adminstration costs specified.
     *
     * @return the adminstration costs specified.
     */
    public BigDecimal getAdminstrationCosts() {
        return adminstrationCosts;
    }

    /**
     * Get the adminstration costs with the discount.
     *
     * @return the adminstration costs with the discount.
     */
    public BigDecimal getAdminstrationCostsWithDiscount() {
        BigDecimal adminstrationCosts = getAdminstrationCosts().subtract(getAdminstrationCostsDiscount());

        // We cannot have a negative price
        if (adminstrationCosts.compareTo(BigDecimal.ZERO) < 0)
            adminstrationCosts = BigDecimal.ZERO;

        return adminstrationCosts.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Set the adminstration costs specified.
     *
     * @param adminstrationCosts The adminstration costs specified.
     */
    public void setAdminstrationCosts(BigDecimal adminstrationCosts) {
        this.adminstrationCosts = adminstrationCosts.setScale(2, RoundingMode.HALF_UP);
    }

    @NotNull
    @Min(0)
    @Digits(integer = 5, fraction = 2)
    @Column(name = "adminstrationcostsdiscount", nullable = false)
    private BigDecimal adminstrationCostsDiscount;

    /**
     * Get the computated discount for the adminstration costs.
     *
     * @return the computated discount for the adminstration costs.
     */
    public BigDecimal getAdminstrationCostsDiscount() {
        return adminstrationCostsDiscount;
    }

    /**
     * Set the computated discount for the adminstration costs.
     *
     * @param discount the computated discount for the adminstration costs.
     */
    public void setAdminstrationCostsDiscount(BigDecimal discount) {
        this.adminstrationCostsDiscount = discount.setScale(2, RoundingMode.HALF_UP);
    }

    @Min(0)
    @Max(100)
    @Column(name = "adminstrationcostsbtwpercentage")
    private Integer adminstrationCostsBtwPercentage;

    /**
     * Get the BTW percentage of the administration costs.
     *
     * @return the BTW percentage of the administration costs.
     */
    public Integer getAdminstrationCostsBtwPercentage() {
        return adminstrationCostsBtwPercentage;
    }

    /**
     * Set the BTW percentage of the administration costs.
     *
     * @param adminstrationCostsBtwPercentage the BTW percentage of the administration costs.
     */
    public void setAdminstrationCostsBtwPercentage(Integer adminstrationCostsBtwPercentage) {
        this.adminstrationCostsBtwPercentage = adminstrationCostsBtwPercentage;
    }

    @Digits(integer = 5, fraction = 2)
    @Column(name = "adminstrationcostsbtwprice")
    private BigDecimal adminstrationCostsBtwPrice;

    /**
     * Get the administration costs price for BTW.
     *
     * @return the administration costs price for BTW.
     */
    public BigDecimal getAdminstrationCostsBtwPrice() {
        return adminstrationCostsBtwPrice;
    }

    /**
     * Set the administration costs price for BTW.
     *
     * @param adminstrationCostsBtwPrice the administration costs price for BTW.
     */
    public void setAdminstrationCostsBtwPrice(BigDecimal adminstrationCostsBtwPrice) {
        if (adminstrationCostsBtwPrice != null)
            adminstrationCostsBtwPrice = adminstrationCostsBtwPrice.setScale(2, RoundingMode.HALF_UP);
        this.adminstrationCostsBtwPrice = adminstrationCostsBtwPrice;
    }

    /**
     * Get the administration costs price without the BTW.
     *
     * @return the administration costs price without the BTW.
     */
    public BigDecimal getAdminstrationCostsWithoutTax() {
        double percentage = (double) this.adminstrationCostsBtwPercentage / 100 + 1;
        BigDecimal percentageDecimal = new BigDecimal(percentage);
        BigDecimal costsWithoutTax = this.adminstrationCosts.subtract(adminstrationCostsDiscount);
        costsWithoutTax = costsWithoutTax.divide(percentageDecimal, 2, RoundingMode.HALF_UP);
        return costsWithoutTax;
    }

    /**
     * The discount specified for this reproduction.
     */
    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "discount_percentage", nullable = false)
    private int discountPercentage;

    /**
     * Get the discount percentage specified.
     *
     * @return The discount percentage specified.
     */
    public int getDiscountPercentage() {
        return discountPercentage;
    }

    /**
     * Set the discount percentage specified.
     *
     * @param discountPercentage The discount percentage specified.
     */
    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    /**
     * The Reproductions's comment.
     */
    @Size(max = 255)
    @Column(name = "comment")
    private String comment;

    /**
     * Get the comment on a Reproduction.
     *
     * @return The comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set the comment on a Reproduction.
     *
     * @param val The value to set the comment to.
     */
    public void setComment(String val) {
        comment = val;
    }

    //    @Enumerated(EnumType.STRING)
    @Column(name = "requestlocale", nullable = false)
    private Locale requestLocale;

    /**
     * Get the locale in which this Reproduction was requested.
     *
     * @return the Reproduction's request locale.
     */
    public Locale getRequestLocale() {
        return requestLocale;
    }

    /**
     * Set the Reproduction's request locale.
     *
     * @param locale the Reproduction's request locale.
     */
    public void setRequestLocale(Locale locale) {
        requestLocale = locale;
    }

    /**
     * A token for the user to access his/her own reproduction.
     */
    @NotBlank
    @Size(max = 36)
    @Column(name = "token", nullable = false)
    private String token;

    /**
     * Get the token.
     *
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    @OneToMany(mappedBy = "reproduction", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoldingReproduction> holdingReproductions;

    public List<HoldingReproduction> getHoldingReproductions() {
        return holdingReproductions;
    }

    public void setHoldingReproductions(List<HoldingReproduction> holdingReproductions) {
        this.holdingReproductions = holdingReproductions;
    }

    /**
     * Returns all holdings assoicated with this request.
     *
     * @return A list of holdings.
     */
    @Override
    public List<Holding> getHoldings() {
        List<Holding> holdings = new ArrayList<>();
        if (holdingReproductions != null) {
            for (HoldingReproduction holdingReproduction : holdingReproductions) {
                holdings.add(holdingReproduction.getHolding());
            }
            return holdings;
        }
        return null;
    }

    /**
     * Returns all HoldingRequests assoicated with this request.
     *
     * @return A list of HoldingRequests.
     */
    @Override
    public List<? extends HoldingRequest> getHoldingRequests() {
        return holdingReproductions;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    // For reading purposes only, to prevent loading the complete order entity
    @Column(name = "order_id", insertable = false, updatable = false)
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    /**
     * Computes the total price of all holdings together (wihout the discount).
     *
     * @return The total price for this reproduction (wihout the discount).
     */
    public BigDecimal getTotalPrice() {
        BigDecimal price = getAdminstrationCosts();

        // First add the price of each holding in this reproduction
        for (HoldingReproduction hr : getHoldingReproductions())
            price = price.add(hr.getCompletePrice());

        // We cannot have a negative price
        if (price.compareTo(BigDecimal.ZERO) < 0)
            price = BigDecimal.ZERO;

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the total discount of all holdings together
     *
     * @return The total discount for this reproduction.
     */
    public BigDecimal getTotalDiscount() {
        BigDecimal price = getAdminstrationCostsDiscount();

        // First add the discount of each holding in this reproduction
        for (HoldingReproduction hr : getHoldingReproductions())
            price = price.add(hr.getDiscount());

        // We cannot have a negative discount
        if (price.compareTo(BigDecimal.ZERO) < 0)
            price = BigDecimal.ZERO;

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the total price of all holdings together (with the discount).
     *
     * @return The total price for this reproduction (with the discount).
     */
    public BigDecimal getTotalPriceWithDiscount() {
        BigDecimal price = getTotalPrice().subtract(getTotalDiscount());

        // We cannot have a negative price
        if (price.compareTo(BigDecimal.ZERO) < 0)
            price = BigDecimal.ZERO;

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the total price (excl. BTW) of all holdings together.
     *
     * @return The total price (excl. BTW) for this reproduction.
     */
    public BigDecimal getTotalPriceExclBTW() {
        BigDecimal price = getTotalPriceWithDiscount().subtract(getTotalBTWPrice());

        // We cannot have a negative price
        if (price.compareTo(BigDecimal.ZERO) < 0)
            price = BigDecimal.ZERO;

        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Computes the total BTW for each BTW percentage.
     *
     * @return The total BTW for each BTW percentage.
     */
    public Map<String, BigDecimal> getTotalBTW() {
        Map<String, BigDecimal> btwTotals = new TreeMap<>();
        for (HoldingReproduction hr : getHoldingReproductions()) {
            BigDecimal totalBtwPrice = BigDecimal.ZERO;
            if (btwTotals.containsKey(hr.getBtwPercentage().toString()))
                totalBtwPrice = btwTotals.get(hr.getBtwPercentage().toString());

            totalBtwPrice = totalBtwPrice.add(hr.getBtwPrice());
            btwTotals.put(hr.getBtwPercentage().toString(), totalBtwPrice.setScale(2, RoundingMode.HALF_UP));
        }

        // Administration costs includes BTW as well
        BigDecimal totalBtwPrice = BigDecimal.ZERO;
        if (btwTotals.containsKey(getAdminstrationCostsBtwPercentage().toString()))
            totalBtwPrice = btwTotals.get(getAdminstrationCostsBtwPercentage().toString());

        totalBtwPrice = totalBtwPrice.add(getAdminstrationCostsBtwPrice());
        btwTotals.put(getAdminstrationCostsBtwPercentage().toString(), totalBtwPrice.setScale(2, RoundingMode.HALF_UP));

        return btwTotals;
    }

    /**
     * Computes the total BTW price.
     *
     * @return The total BTW price.
     */
    public BigDecimal getTotalBTWPrice() {
        BigDecimal totalBtwPrice = BigDecimal.ZERO;
        for (BigDecimal btwPrice : getTotalBTW().values()) {
            totalBtwPrice = totalBtwPrice.add(btwPrice);
        }

        return totalBtwPrice.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Whether this reproduction is for free.
     *
     * @return If this reproduction is for free.
     */
    public boolean isForFree() {
        return (getTotalPriceWithDiscount().compareTo(BigDecimal.ZERO) == 0);
    }

    /**
     * Computes the estimated delivery time of all holdings together.
     *
     * @return The estimated delivery time for this reproduction.
     */
    public int getEstimatedDeliveryTime() {
        List<HoldingReproduction> holdingReproductions = getHoldingReproductions();

        // When there are more than 10 items, repro requires about 2 weeks
        if (holdingReproductions.size() > 10)
            return 14;

        // Otherwise, determine delivery time by the item with the longest estimated delivery time
        int longestDeliveryTime = 0;
        for (HoldingReproduction hr : holdingReproductions) {
            if (hr.getDeliveryTime() > longestDeliveryTime)
                longestDeliveryTime = hr.getDeliveryTime();
        }
        return longestDeliveryTime;
    }

    /**
     * Determine if all holdings of this reproduction are already in the SOR.
     *
     * @return Whether all holdings of this reproduction are already in the SOR.
     */
    public boolean isCompletelyInSor() {
        for (HoldingReproduction hr : getHoldingReproductions()) {
            if (!hr.isInSor())
                return false;
        }
        return true;
    }

    @Override
    public Request mergeWith(Request request) {
        if (request instanceof Reproduction) {
            final Reproduction reproduction = (Reproduction) request;
            this.adminstrationCosts = reproduction.getAdminstrationCosts();
            this.adminstrationCostsBtwPercentage = reproduction.getAdminstrationCostsBtwPercentage();
            this.adminstrationCosts = reproduction.getAdminstrationCosts();
            this.adminstrationCostsDiscount = reproduction.getAdminstrationCostsDiscount();
            this.comment = reproduction.getComment();
            this.creationDate = reproduction.getCreationDate();
            this.customerEmail = reproduction.getCustomerEmail();
            this.customerName = reproduction.getCustomerName();
            this.date = reproduction.getDate();
            this.dateHasOrderDetails = reproduction.getDateHasOrderDetails();
            this.datePaymentAccepted = reproduction.getDatePaymentAccepted();
            this.discountPercentage = reproduction.getDiscountPercentage();
            this.order = reproduction.getOrder();
            this.orderId = reproduction.getOrderId();
            this.token = reproduction.getToken();
        } else {
            final Reservation reservation = (Reservation) request;
            switch (reservation.getStatus()) {
                case ACTIVE:
                    setStatus(Status.ACTIVE);
                    break;
                case COMPLETED:
                    setStatus(Status.COMPLETED);
                    break;
                case PENDING:
                    setStatus(Status.WAITING_FOR_ORDER_DETAILS);
                    break;
            }
            this.comment = reservation.getComment();
            this.creationDate = reservation.getCreationDate();
            this.date = reservation.getDate();
            this.customerEmail = reservation.getVisitorEmail(); // of reservation.getEmail(); ?
            this.customerName = reservation.getVisitorName();
        }

        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reproduction) {
            Reproduction other = (Reproduction) obj;
            if ((this.getId() != 0) && (other.getId() != 0))
                return (this.getId() == other.getId());
        }
        return super.equals(obj);
    }

    /**
     * Set default data for Reproductions.
     */
    public Reproduction() {
        setStatus(Status.WAITING_FOR_ORDER_DETAILS);
        setDate(new Date());
        setCreationDate(new Date());
        setDiscountPercentage(0);
        setAdminstrationCosts(BigDecimal.ZERO);
        setAdminstrationCostsDiscount(BigDecimal.ZERO);
        setAdminstrationCostsBtwPercentage(0);
        setAdminstrationCostsBtwPrice(BigDecimal.ZERO);
        setRequestLocale(LocaleContextHolder.getLocale());
        holdingReproductions = new ArrayList<>();
        token = UUID.randomUUID().toString();
    }
}
