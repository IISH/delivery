package org.socialhistoryservices.delivery.reproduction.entity;

import org.socialhistoryservices.delivery.api.PayWayMessage;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Order object representing a Order from PayWay.
 */
@Entity
@Table(name = "orders")
@Configurable
public class Order {
    public static final int ORDER_NOT_PAYED = 0;
    public static final int ORDER_PAYED = 1;
    public static final int ORDER_REFUND_OGONE = 2;
    public static final int ORDER_REFUND_BANK = 3;

    public static final int ORDER_OGONE_PAYMENT = 0;
    public static final int ORDER_BANK_PAYMENT = 1;
    public static final int ORDER_CASH_PAYMENT = 2;

    public static final int PAYMENT_ACCEPTED = 1;
    public static final int PAYMENT_DECLINED = 2;
    public static final int PAYMENT_EXCEPTION = 3;
    public static final int PAYMENT_CANCELLED = 4;
    public static final int PAYMENT_OTHER_STATUS = 5;

    /**
     * The Order's id.
     */
    @Id
    @Column(name = "id")
    private long id;

    /**
     * Get the Order's id.
     *
     * @return the Order's id.
     */
    public long getId() {
        return id;
    }

    /**
     * Set the Order's id.
     *
     * @param id the Order's id.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * The order code
     */
    @Size(max = 50)
    @Column(name = "ordercode", unique = true)
    private String orderCode;

    /**
     * Get the order code.
     *
     * @return the order code.
     */
    public String getOrderCode() {
        return orderCode;
    }

    /**
     * Set the order code.
     *
     * @param orderCode the order code.
     */
    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    @Min(0)
    @NotNull
    @Column(name = "amount", nullable = false)
    private long amount;

    /**
     * Get the Order's amount.
     *
     * @return the Order's amount.
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Get the Order's amount as a BigDecimal.
     *
     * @return the Order's amount as a BigDecimal.
     */
    public BigDecimal getAmountAsBigDecimal() {
        return new BigDecimal(amount).movePointLeft(2);
    }

    /**
     * Set the Order's amount.
     *
     * @param amount the Order's amount.
     */
    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Min(0)
    @NotNull
    @Column(name = "refundedamount", nullable = false)
    private long refundedAmount;

    /**
     * Get the Order's refunded amount.
     *
     * @return the Order's refunded amount.
     */
    public long getRefundedAmount() {
        return refundedAmount;
    }

    /**
     * Get the Order's refunded amount as a BigDecimal.
     *
     * @return the Order's refunded amount as a BigDecimal.
     */
    public BigDecimal getRefundedAmountAsBigDecimal() {
        return new BigDecimal(refundedAmount).movePointLeft(2);
    }

    /**
     * Set the Order's refunded amount.
     *
     * @param refundedAmount the Order's refunded amount.
     */
    public void setRefundedAmount(long refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    @NotNull
    @Column(name = "payed", nullable = false)
    private int payed;

    /**
     * Get the Order's payment status.
     *
     * @return the Order's payment status.
     */
    public int getPayed() {
        return payed;
    }

    /**
     * Set the Order's payment status.
     *
     * @param payed the Order's payment status.
     */
    public void setPayed(int payed) {
        this.payed = payed;
    }

    @NotNull
    @Column(name = "paymentmethod", nullable = false)
    private int paymentMethod;

    /**
     * Get the Order's payment method.
     *
     * @return the Order's payment method.
     */
    public int getPaymentMethod() {
        return paymentMethod;
    }

    /**
     * Set the Order's payment method.
     *
     * @param paymentMethod the Order's payment method.
     */
    public void setPaymentMethod(int paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    /**
     * The Order's creation date.
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdat", nullable = false)
    private Date createdAt;

    /**
     * Get the Order's creation date.
     *
     * @return the Order's creation date.
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the Order's creation date.
     *
     * @param createdAt the Order's creation date.
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * The Order's update date.
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updatedat", nullable = false)
    private Date updatedAt;

    /**
     * Get the Order's update date.
     *
     * @return the Order's update date.
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the Order's update date.
     *
     * @param updatedAt the Order's update date.
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * The Order's refund date.
     */
    @Column(name = "refundedat")
    private Date refundedAt;

    /**
     * Get the Order's refund date.
     *
     * @return the Order's refund date.
     */
    public Date getRefundedAt() {
        return refundedAt;
    }

    /**
     * Set the Order's refund date.
     *
     * @param refundedAt the Order's refund date.
     */
    public void setRefundedAt(Date refundedAt) {
        this.refundedAt = refundedAt;
    }

    /**
     * The Order's description
     */
    @Size(max = 100)
    @Column(name = "description")
    private String description;

    /**
     * Get the Order's description.
     *
     * @return the Order's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the Order's description.
     *
     * @param description the Order's description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Reproduction reproduction;

    public Reproduction getReproduction() {
        return reproduction;
    }

    public void setReproduction(Reproduction reproduction) {
        this.reproduction = reproduction;
    }

    /**
     * Maps the order details from a PayWay message to this order.
     *
     * @param message The PayWay message.
     */
    public void mapFromPayWayMessage(PayWayMessage message) {
        this.id = message.getInteger("orderid");
        this.orderCode = message.getString("ordercode");
        this.amount = message.getLong("amount");
        this.refundedAmount = (message.getLong("refundedamount") != null) ? message.getLong("refundedamount") : 0L;
        this.payed = message.getInteger("payed");
        this.paymentMethod = message.getInteger("paymentmethod");
        this.createdAt = message.getDate("createdat");
        this.updatedAt = message.getDate("updatedat");
        this.refundedAt = message.getDate("refundedat");
        this.description = message.getString("com");
    }

    /**
     * Set default data for Orders.
     */
    public Order() {
        setAmount(0L);
        setRefundedAmount(0L);
        setPayed(ORDER_NOT_PAYED);
        setPaymentMethod(ORDER_OGONE_PAYMENT);
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
        setRefundedAt(new Date());
    }
}
