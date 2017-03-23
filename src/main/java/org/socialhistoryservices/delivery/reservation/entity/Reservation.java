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
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reservation object representing a reservation that can be made on a set of
 * records.
 */
@Entity
@Table(name="reservations")
@JsonIgnoreProperties(ignoreUnknown = true)
@Configurable
public class Reservation extends Request {

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

    /**
     * Returns the name of the person making the request.
     *
     * @return The name of the person.
     */
    @Override
    public String getName() {
        return getVisitorName();
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

    /**
     * Returns the email address of the person making the request.
     *
     * @return The email address of the person.
     */
    @Override
    public String getEmail() {
        return getVisitorEmail();
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
    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set the Reservation's date.
     * @param creationDate the Reservation's date.
     */
    @Override
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

    @OneToMany(mappedBy="reservation", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<HoldingReservation> holdingReservations;


    public void setHoldingReservations(List<HoldingReservation> hrs) {
        holdingReservations = hrs;
    }

    public List<HoldingReservation> getHoldingReservations() {
        return holdingReservations;
    }

    /**
     * Returns all holdings assoicated with this request.
     *
     * @return A list of holdings.
     */
    @Override
    public List<Holding> getHoldings() {
            List<Holding> holdings = new ArrayList<Holding>();
            if (holdingReservations != null) {
                    for (HoldingReservation holdingReservation : holdingReservations) {
                            holdings.add(holdingReservation.getHolding());
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Reservation) {
            Reservation other = (Reservation) obj;
            if ((this.getId() != 0) && (other.getId() != 0))
                return (this.getId() == other.getId());
        }
        return super.equals(obj);
    }

    /**
     * Set default data for reservations.
     */
    public Reservation() {
        setSpecial(false);
        setStatus(Status.PENDING);
        setCreationDate(new Date());
        holdingReservations = new ArrayList<HoldingReservation>();
    }
}
