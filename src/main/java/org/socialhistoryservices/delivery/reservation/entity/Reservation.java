package org.socialhistoryservices.delivery.reservation.entity;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.socialhistoryservices.delivery.permission.entity.Permission;
import org.socialhistoryservices.delivery.record.entity.Holding;
import org.socialhistoryservices.delivery.request.entity.HoldingRequest;
import org.socialhistoryservices.delivery.request.entity.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.*;

/**
 * Reservation object representing a reservation that can be made on a set of
 * records.
 */
@Entity
@Table(name="reservations")
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
    @Column(name="visitorname", nullable=false)
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
    @Pattern(regexp=".+@.{2,}\\..{2,}", message="E-mail address is invalid")
    @Column(name="visitoremail", nullable=false)
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
    @Column(name="return_date")
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
            List<Holding> holdings = new ArrayList<>();
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

    /** The Reservation's permissions. */
    @ManyToMany
    @JoinTable(name="reservation_permissions",
        joinColumns=@JoinColumn(name="reservation_id"),
        inverseJoinColumns=@JoinColumn(name="permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Get the Reservation's permissions.
     * @return the Reservation's permissions.
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Set the Reservation's permissions.
     * @param permissions the Reservation's permissions.
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /** The Reservation's comment. */
    @Size(max=255)
    @Column(name="comment")
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
        setStatus(Status.PENDING);
        setCreationDate(new Date());
        holdingReservations = new ArrayList<>();
    }
}
