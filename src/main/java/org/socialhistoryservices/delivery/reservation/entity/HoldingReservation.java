package org.socialhistoryservices.delivery.reservation.entity;

import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * Reservation object representing a reservation that can be made on a set of
 * records.
 */
@Entity
@Table(name = "holding_reservations", indexes = {@Index(columnList = "completed", name = "holding_reservations_completed_idx"),
                                                @Index(columnList = "holding_id", name = "holding_reservations_holding_fk"),
                                                @Index(columnList = "reservation_id", name = "holding_reservations_reservation_fk")})
public class HoldingReservation extends HoldingRequest {


    /** The Reservation's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Reservation's id.
     * @return the Reservation's id.
     */
    @Override
    public int getId() {
        return id;
    }

    /** Whether the reservation holding has been printed or not. */
    @Column(name="printed")
    private boolean printed = false;

    /**
     * Set whether the reservation holding was printed or not.
     * @param b True to consider the reservation holding to be printed at least once,
     * false otherwise.
     */
    @Override
    public void setPrinted(boolean b) {
        printed = b;
    }

    /**
     * Check if the reservation holding (is considered) to be printed at least once.
     * @return True if the reservation holding was printed at least once,
     * false otherwise.
     */
    @Override
    public boolean isPrinted() {
        return printed;
    }

    /** Is the holding completed for this reservation? */
    @Column(name="completed", nullable=false)
    private boolean completed = false;

    /**
     * Is the holding completed for this reservation?
     * @return Whether this holding is completed for this reservation.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set the holding completed for this reservation?
     * @param completed Whether the holding has been completed for this reservation.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /** The Reservation's name. */
    @Size(max=255)
    @Column(name="comment")
    private String comment;

    /**
     * Get the comment on a specific holding in a reservation.
     * @return The comment.
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Set the comment on a specific holding in a reservation.
     * @param val The value to set the comment to.
     */
    @Override
    public void setComment(String val) {
        comment = val;
    }

    /** The RecordPermission's permission. */
    @ManyToOne
    @JoinColumn(name="reservation_id")
    private Reservation reservation;

    /**
     * Get the HoldingReservation's reservation.
     * @return the HoldingReservation's reservation.
     */
    public Reservation getReservation() {
        return reservation;
    }

    /**
     * Set the HoldingReservation's reservation.
     * @param res the HoldingReservation's reservation.
     */
    public void setReservation(Reservation res) {
        this.reservation = res;
    }

    /**
     * Get the HoldingRequest's request.
     *
     * @return the HoldingRequest's request.
     */
    @Override
    public Request getRequest() {
        return getReservation();
    }

    /**
     * Set the HoldingRequest's request.
     *
     * @param request
     */
    @Override
    public void setRequest(Request request) {
        setReservation((Reservation) request);
    }

    /** The HoldingReservation's holding. */
    @ManyToOne
    @JoinColumn(name="holding_id")
    private Holding holding;

    /**
     * Get the HoldingReservation's holding.
     * @return the HoldingReservation's holding.
     */
    @Override
    public Holding getHolding() {
        return holding;
    }

    /**
     * Set the HoldingReservation's holding.
     * @param h the HoldingReservation's holding.
     */
    @Override
    public void setHolding(Holding h) {
        this.holding = h;
    }
}
