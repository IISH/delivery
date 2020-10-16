package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException;
import org.springframework.validation.BindingResult;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.Calendar;
import java.util.List;

public interface ReservationDateExceptionService {
    /**
     * Add a ReservationDateException to the database.
     *
     * @param obj ReservationDateException to add.
     */
    void addReservationDateException(ReservationDateException obj);

    /**
     * Remove a ReservationDateException from the database.
     *
     * @param obj ReservationDateException to remove.
     */
    void removeReservationDateException(ReservationDateException obj);

    /**
     * List all ReservationDateExceptions matching a built query.
     *
     * @param q The criteria query to execute.
     * @return A list of matching ReservationDateExceptions.
     */
    List<ReservationDateException> listReservationDateExceptions(CriteriaQuery<ReservationDateException> q);

    /**
     * Get a criteria builder for querying ReservationDateExceptions.
     *
     * @return the CriteriaBuilder.
     */
    CriteriaBuilder getReservationDateExceptionCriteriaBuilder();

    /**
     * Get a ReservationDateException by id of the date exception.
     *
     * @param id The id to search the ReservationDateException on.
     * @return A ReservationDateException matching the given id.
     */
    ReservationDateException getReservationDateExceptionsById(int id);

    /**
     * Checks whether the given ReservationDateException is valid. This by checking the description
     * and/or the start date not being null.
     *
     * @param resExcept The given ReservationDateException to check.
     * @param result    The result of the check.
     * @return A boolean value representing whether the ReservationDateException is valid.
     */
    Boolean isValid(ReservationDateException resExcept, BindingResult result);

    /**
     * Get all the ReservationDateException's dates known in the database.
     *
     * @return all ReservationDateException's dates from the database.
     */
    List<Calendar> getExceptionDates();

    /**
     * Get all the ReservationDateExceptions from the database.
     *
     * @return all ReservationDateExceptions from the database.
     */
    List<ReservationDateException> getReservationDateExceptions();

    /**
     * Get the reason for the ReservationDateException for the corresponding Calendar date.
     *
     * @param cal Calendar with the date to be checked.
     * @return the reason for the ReservationDateException.
     */
    String getReasonForExceptionDate(Calendar cal);

    /**
     * Checks whether the given ReservationDateException exists in the database. This by checking
     * if the (one of) the dates are already in use by another ReservationDateException.
     *
     * @param resExcept The ReservationDateException to check.
     * @param result    The BindingResult to hold any errors.
     * @return A boolean value representing whether the ReservationDateException already exists.
     */
    Boolean exceptionDateExists(ReservationDateException resExcept, BindingResult result);

    /**
     * Checks whether the end date of the ReservationDateException is before the begin date.
     *
     * @param resExcept The ReservationDateException to check.
     * @param result    The BindingResult to hold any errors.
     * @return A boolean value representing whether the end date is before the begin date.
     */
    Boolean isEndDateBeforeBeginDate(ReservationDateException resExcept, BindingResult result);
}
