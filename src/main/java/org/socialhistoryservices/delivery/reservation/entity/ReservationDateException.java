package org.socialhistoryservices.delivery.reservation.entity;

import org.springframework.beans.factory.annotation.Configurable;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "reservation_date_exceptions")
@Configurable
public class ReservationDateException {
    /**
     * The ReservationDateException's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the ReservationDateException's id
     *
     * @return the ReservationDateException's id
     */
    public int getId(){return id;}

    /**
     * The ReservationDateException's startDate
     */
    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "exception_startdate", nullable = false)
    private Date startDate;

    /**
     * Get the ReservationDateException's startDate
     *
     * @return the ReservationDateException's startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set the ReservationDateException's startDate
     *
     * @param startDate the ReservationDateException's startDate
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * The ReservationDateException's endDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "exception_enddate")
    private Date endDate;

    /**
     * Get the ReservationDateException's endDate.
     *
     * @return the ReservationDateException's endDate.
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set the ReservationDateException's endDate.
     *
     * @param endDate the ReservationDateException's endDate.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * The ReservationDateException's description
     */
    @NotBlank
    @Size(max = 255)
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * Get the ReservationDateException's description
     *
     * @return the ReservationDateException's description
     */
    public String getdescription() {
        return description;
    }

    /**
     * Set the ReservationDateException's description
     *
     * @param description the ReservationDateException's description
     */
    public void setdescription(String description) {
        this.description = description;
    }

    /**
     * Get the ReservationDateException's dates.
     *
     * @return the ReservationDateException's dates.
     */
    public List<Date> getDatesOfReservationDateException() {
        List<Date> dates = new ArrayList<>();
        if (this.endDate != null) {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            while (startCal.getTime().before(endDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startCal.getTime());
                dates.add(calendar.getTime());
                startCal.add(Calendar.DAY_OF_YEAR, 1);
            }
            dates.add(endDate);

        }
        else {
            dates.add(startDate);
        }
        return dates;
    }

    /**
     * Get the ReservationDateException's data as a string
     *
     * @return the ReservationDateException's data as a string
     */
    @Override
    public String toString() {
        return "Id: " + id + " Start date: " + startDate + " End date: " + endDate + " Reason: " + description;
    }
}
