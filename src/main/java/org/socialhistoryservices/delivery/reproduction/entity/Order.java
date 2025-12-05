package org.socialhistoryservices.delivery.reproduction.entity;

import com.mollie.mollie.models.components.PaymentResponse;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static com.mollie.mollie.models.components.PaymentResponseStatus.*;

/**
 * Order object representing an Order from PayWay or Mollie.
 */
@Entity
@Table(name = "orders")
@Configurable
public class Order {
    public static final int ORDER_NOT_PAYED = 0;
    public static final int ORDER_PAYED = 1;
    public static final int ORDER_REFUND = 2;

    /**
     * The Order's id.
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * Get the Order's id.
     *
     * @return the Order's id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the Order's id.
     *
     * @param id the Order's id.
     */
    public void setId(String id) {
        this.id = id;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private Provider provider;

    public enum Provider {
        PAYWAY,
        MOLLIE
    }

    /**
     * Get the Order's provider.
     *
     * @return the Order's provider.
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Set the Order's provider.
     *
     * @param provider the Order's provider.
     */
    public void setProvider(Provider provider) {
        this.provider = provider;
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

    @Column(name = "checkouturl")
    private String checkoutUrl;

    /**
     * Get the Order's checkout url.
     *
     * @return the Order's checkout url.
     */
    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    /**
     * Set the Order's checkout url.
     *
     * @param checkoutUrl the Order's checkout url.
     */
    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
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
     * Maps the order details from a Mollie Payment to this order.
     *
     * @param payment The Mollie Payment.
     */
    public void mapFromPayment(PaymentResponse payment) {
        this.id = payment.id();
        this.description = payment.description();

        this.amount = new BigDecimal(payment.amount().value()).movePointRight(2).longValue();
        payment.amountRefunded().ifPresent(refundedAmount ->
                this.refundedAmount = new BigDecimal(refundedAmount.value()).movePointRight(2).longValue());

        if (payment.status() == PAID || payment.status() == AUTHORIZED)
            this.payed = this.refundedAmount >= this.amount ? ORDER_REFUND : ORDER_PAYED;
        else
            this.payed = ORDER_NOT_PAYED;

        payment.links().checkout().ifPresent(checkout ->
                this.checkoutUrl = checkout.href());

        this.createdAt = Date.from(ZonedDateTime.parse(payment.createdAt(), DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant());
        this.updatedAt = Date.from(Stream.of(payment.createdAt(), payment.authorizedAt().orElse(null),
                        payment.paidAt().orElse(null), payment.canceledAt().orElse(null),
                        payment.expiredAt().orElse(null), payment.failedAt().orElse(null))
                .filter(Objects::nonNull)
                .map(s -> ZonedDateTime.parse(s, DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .max(naturalOrder()).get().toInstant());

        if (this.refundedAt == null && this.refundedAmount > 0)
            this.refundedAt = new Date();
    }

    /**
     * Set default data for Orders.
     */
    public Order() {
        setAmount(0L);
        setRefundedAmount(0L);
        setPayed(ORDER_NOT_PAYED);
        setProvider(Provider.MOLLIE);
        setCreatedAt(new Date());
        setUpdatedAt(new Date());
    }
}
