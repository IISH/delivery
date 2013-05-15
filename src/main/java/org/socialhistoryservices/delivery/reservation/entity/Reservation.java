/**
 * Copyright (C) 2013 International Institute of Social History
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.socialhistoryservices.delivery.reservation.entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Cascade;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Reservation object representing a reservation that can be made on a set of
 * records.
 */
@Entity
@Table(name="reservations")
@JsonIgnoreProperties(ignoreUnknown = true)
@Configurable
public class Reservation {

    /** Status of the reservation. */
    public enum Status {
        PENDING,
        ACTIVE,
        COMPLETED
    }

    /** The Reservation's id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the Reservation's id.
     * @return the Reservation's id.
     */
    public int getId() {
        return id;
    }

    /** The Reservation's name. */
    @NotBlank
    @Size(max=255)
    @Column(name="visitorName", nullable=false)
    private String visitorName;

    /**
     * Get the Reservation's name.
     * @return the Reservation's name.
     */
    public String getVisitorName() {
        return visitorName;
    }

    /**
     * Set the Reservation's name.
     * @param name the Reservation's name.
     */
    public void setVisitorName(String name) {
        this.visitorName = name;
    }

    /** The Reservation's email. */
    @NotBlank
    @Size(max=255)
    @Email
    @Column(name="visitorEmail", nullable=false)
    private String visitorEmail;

    /**
     * Get the Reservation's email.
     * @return the Reservation's email.
     */
    public String getVisitorEmail() {
        return visitorEmail;
    }

    /**
     * Set the Reservation's email.
     * @param email the Reservation's email.
     */
    public void setVisitorEmail(String email) {
        this.visitorEmail = email;
    }

    /** The Reservation's date. */
    @NotNull
    //@ValidReservationDate
    @Temporal(TemporalType.DATE)
    @Column(name="date", nullable=false)
    private Date date;

    /**
     * Get the Reservation's date.
     * @return the Reservation's date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Set the Reservation's date.
     * @param date the Reservation's date.
     */
    public void setDate(Date date) {
        this.date = date;
    }



    /** The Reservation's return date (currently optional). */
    @Temporal(TemporalType.DATE)
    @Column(name="return_date", nullable=true)
    private Date returnDate;

    /**
     * Get the Reservation's return date (currently optional).
     * @return the Reservation's date.
     */
    public Date getReturnDate() {
        return returnDate;
    }

    /**
     * Set the Reservation's return date (currently optional).
     * @param date the Reservation's date.
     */
    public void setReturnDate(Date date) {
        this.returnDate = date;
    }

    /** The Reservation's creation date. */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creation_date", nullable=false)
    private Date creationDate;

    /**
     * Get the Reservation's creation date.
     * @return the Reservation's creation date.
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the Reservation's date.
     * @param creationDate the Reservation's date.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /** The Reservation's special. */
    @Column(name="special", nullable=false)
    private boolean special;

    /**
     * Get the Reservation's special.
     * @return the Reservation's special.
     */
    public boolean getSpecial() {
        return special;
    }

    /**
     * Set the Reservation's special.
     * @param special the Reservation's special.
     */
    public void setSpecial(boolean special) {
        this.special = special;
    }

    /** The Reservation's status. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private Status status;

    /**
     * Get the Reservation's status.
     * @return the Reservation's status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set the Reservation's status.
     * @param status the Reservation's status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    @OneToMany(mappedBy="reservation", cascade=CascadeType.ALL)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    private List<HoldingReservation> holdingReservations;


    public void setHoldingReservations(List<HoldingReservation> hrs) {
        holdingReservations = hrs;
    }

    public List<HoldingReservation> getHoldingReservations() {
        return holdingReservations;
    }

    /** The Reservation's permission. */
    @ManyToOne
    @JoinColumn(name="permission_id")
    private Permission permission;

    /**
     * Get the Reservation's permission.
     * @return the Reservation's permission.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Set the Reservation's permission.
     * @param permission the Reservation's permission.
     */
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    /** The number of this reservation in the queue. */
    @Column(name="queueNo")
    private Integer queueNo;
    /**
     * Get the Reservation's queueNo.
     * @return The queueNo.
     */
    public Integer getQueueNo() {
        return queueNo;
    }
    /**
     * Set the Reservation's queueNo.
     * @param queueNo The new queueNo.
     */
    public void setQueueNo(Integer queueNo) {
        this.queueNo = queueNo;
    }

    /** Whether the reservation has been printed or not. */
    @Column(name="printed")
    private boolean printed;

    /**
     * Set whether the reservation was printed or not.
     * @param b True to consider the reservation to be printed at least once,
     * false otherwise.
     */
    public void setPrinted(boolean b) {
        printed = b;
    }

    /**
     * Check if the reservation (is considered) to be printed at least once.
     * @return True if the reservation was printed at least once,
     * false otherwise.
     */
    public boolean isPrinted() {
        return printed;
    }

    /** The Reservation's comment. */
    @Size(max=255)
    @Column(name="comment", nullable=true)
    private String comment;

    /**
     * Get the comment on a reservation.
     * @return The comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Set the comment on a reservation.
     * @param val The value to set the comment to.
     */
    public void setComment(String val) {
        comment = val;
    }

    /**
     * Merge the other reservation's fields into this reservation.
     * @param other The other reservation.
     */
    public void mergeWith(Reservation other) {
        setDate(other.getDate());
        setReturnDate(other.getReturnDate());
        setPrinted(other.isPrinted());
        setSpecial(other.getSpecial());
        setVisitorName(other.getVisitorName());
        setVisitorEmail(other.getVisitorEmail());
        setComment(other.getComment());
        //setPermission(other.getPermission());
        //setQueueNo(other.getQueueNo());



        if (other.getHoldingReservations() == null) {
            for (HoldingReservation hr : getHoldingReservations()) {
                hr.getHolding().setStatus(Holding.Status.AVAILABLE);
            }
            setHoldingReservations(new ArrayList<HoldingReservation>());
        } else {
            // Delete holdings that were not provided.
            deleteHoldingsNotInProvidedReservation(other);

            // Add/update provided.
            addOrUpdateHoldingsProvidedByReservation(other);
        }
        updateStatusAndAssociatedHoldingStatus(other.getStatus());
    }

    /**
     * Add/Update the holdings provided by the provided reservation.
     * @param other The provided reservation.
     */
    private void addOrUpdateHoldingsProvidedByReservation(Reservation other) {
        for (HoldingReservation hr : other.getHoldingReservations()) {
            Holding h = hr.getHolding();
            boolean has = false;
            for (HoldingReservation hr2 : getHoldingReservations()) {
                Holding h2 = hr2.getHolding();
                
                if (h.getSignature().equals(h2.getSignature())
                        && h.getRecord().equals(h2.getRecord())) {
                    has = true;

                    // Update comment and such.
                    hr2.mergeWith(hr);
                }
            }

            if (!has) {

                holdingReservations.add(hr);
            }
        }
    }

    /**
     * Remove the holdings from this record, which are not in the other record.
     * @param other The other record.
     */
    private void deleteHoldingsNotInProvidedReservation(Reservation other) {
        Iterator<HoldingReservation> it = getHoldingReservations().iterator();
        while (it.hasNext()) {
            HoldingReservation hr = it.next();
            Holding h = hr.getHolding();

            boolean has = false;
            for (HoldingReservation hr2 : other.getHoldingReservations()) {
                Holding h2 = hr2.getHolding();
                if (h.getSignature().equals(h2.getSignature())
                        && h.getRecord().equals(h2.getRecord())) {
                    has = true;
                    break;
                }
            }

            if (!has) {
                h.setStatus(Holding.Status.AVAILABLE);
                it.remove();
            }
        }
    }


    /**
     * Set the reservation status and update the associated holdings status
     * accordingly. Only updates status forward.
     * @param status The reservation which changed status.
     */
    public void updateStatusAndAssociatedHoldingStatus(Status status) {
        if (status.ordinal() < getStatus().ordinal()) {
            return;
        }
        setStatus(status);
        Holding.Status hStatus;
        switch (status) {
            case PENDING:
                hStatus = Holding.Status.RESERVED;
                break;
            case ACTIVE:
                hStatus = Holding.Status.IN_USE;
            break;
            default:
                hStatus = Holding.Status.AVAILABLE;
            break;
        }
        for (HoldingReservation hr : getHoldingReservations()) {
            hr.getHolding().setStatus(hStatus);
        }
    }


    /**
     * Set default data for reservations.
     */
    public Reservation() {
        setSpecial(false);
        setStatus(Status.PENDING);
        setCreationDate(new Date());
        setPrinted(false);
        holdingReservations = new ArrayList<HoldingReservation>();
    }
}