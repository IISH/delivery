package org.socialhistoryservices.delivery.reservation.service;

import org.socialhistoryservices.delivery.reservation.dao.ReservationDateExceptionDAO;
import org.socialhistoryservices.delivery.reservation.entity.ReservationDateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

@Service
@Transactional
public class ReservationDateExceptionServiceImpl implements ReservationDateExceptionService {
    @Autowired
    private ReservationDateExceptionDAO reservationDateExceptionDAO;

    @Autowired
    protected MessageSource messageSource;

    /**
     * Add a ReservationDateException to the database.
     *
     * @param obj ReservationDateException to add.
     */
    public void addReservationDateException(ReservationDateException obj) {
        reservationDateExceptionDAO.add(obj);
    }

    /**
     * Remove a ReservationDateException from the database.
     *
     * @param obj ReservationDateException to remove.
     */
    public void removeReservationDateException(ReservationDateException obj) {
        reservationDateExceptionDAO.remove(obj);
    }

    /**
     * List all ReservationDateExceptions matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching ReservationDateExceptions.
     */
    public List<ReservationDateException> listReservationDateExceptions(CriteriaQuery<ReservationDateException> q) {
        return reservationDateExceptionDAO.list(q);
    }

    /**
     * Get a criteria builder for querying ReservationDateExceptions.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getReservationDateExceptionCriteriaBuilder() {
        return reservationDateExceptionDAO.getCriteriaBuilder();
    }

    /**
     * Get a ReservationDateException by id of the date exception.
     *
     * @param id The id to search the ReservationDateException on.
     * @return A ReservationDateException matching the given id.
     */
    public ReservationDateException getReservationDateExceptionsById(int id) {
        return reservationDateExceptionDAO.getById(id);
    }

    /**
     * Get all the ReservationDateException's dates known in the database.
     *
     * @return all ReservationDateException's dates from the database.
     */
    public List<Calendar> getExceptionDates() {
        List<ReservationDateException> result = getReservationDateExceptions();
        List<Calendar> exceptionDates = new ArrayList<>();
        for (ReservationDateException res : result) {
            if (res.getEndDate() == null) {
                Calendar cal = GregorianCalendar.getInstance();
                cal.setTime(res.getStartDate());
                exceptionDates.add(cal);
            }
            else {
                Calendar startCal = GregorianCalendar.getInstance();
                Calendar endCal = GregorianCalendar.getInstance();
                startCal.setTime(res.getStartDate());
                endCal.setTime(res.getEndDate());
                while (startCal.getTime().before(endCal.getTime())) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(startCal.getTime());
                    exceptionDates.add(cal);
                    startCal.add(Calendar.DAY_OF_YEAR, 1);
                }
                exceptionDates.add(endCal);
            }
        }
        return exceptionDates;
    }

    /**
     * Get all the ReservationDateExceptions from the database.
     *
     * @return all ReservationDateExceptions from the database.
     */
    public List<ReservationDateException> getReservationDateExceptions() {
        CriteriaBuilder builder = getReservationDateExceptionCriteriaBuilder();
        CriteriaQuery<ReservationDateException> query = builder.createQuery(ReservationDateException.class);
        Root<ReservationDateException> root = query.from(ReservationDateException.class);
        query.select(root);
        return listReservationDateExceptions(query);
    }

    /**
     * Get the reason for the ReservationDateException for the corresponding Calendar date.
     *
     * @param cal Calendar with the date to be checked.
     * @return the reason for the ReservationDateException.
     */
    public String getReasonForExceptionDate(Calendar cal) {
        List<ReservationDateException> reservationDateExceptions = getReservationDateExceptions();
        for (ReservationDateException res : reservationDateExceptions) {
            for (int i = 0; i < res.getDatesOfReservationDateException().size(); i++) {
                if (cal.getTime().equals(res.getDatesOfReservationDateException().get(i))) { // Make a get dates method for ReservationDateExceptions
                    return res.getdescription();
                }
            }
        }
        return "No reason given!";
    }

    /**
     * Checks whether the given ReservationDateException is valid. This by checking the description
     * and/or the start date not being null.
     *
     * @param resExcept The given ReservationDateException to check.
     * @param result    The result of the check.
     * @return A boolean value representing whether the ReservationDateException is valid.
     */
    public Boolean isValid(ReservationDateException resExcept, BindingResult result) {
        if (resExcept.getdescription() == null) {
            String msg = messageSource.getMessage("reservationDateException.descriptionNotValid", new Object[]{},
                    LocaleContextHolder.getLocale());
            result.addError(new FieldError(result.getObjectName(), "description",
                    resExcept.getdescription(), false,
                    null, null, msg));
            return false;
        }
        else if (resExcept.getStartDate() == null) {
            String msg = messageSource.getMessage("reservationDateException.startDateNotValid", new Object[]{},
                    LocaleContextHolder.getLocale());
            result.addError(new FieldError(result.getObjectName(), "startDate",
                    resExcept.getStartDate(), false,
                    null, null, msg));
            return false;
        }
        else if (exceptionDateExists(resExcept, result)) {
            return false;
        }
        else return !isEndDateBeforeBeginDate(resExcept, result);
    }

    /**
     * Checks whether the given ReservationDateException exists in the database. This by checking
     * if the (one of) the dates are already in use by another ReservationDateException.
     *
     * @param resExcept The ReservationDateException to check.
     * @param result    The BindingResult to hold any errors.
     * @return A boolean value representing whether the ReservationDateException already exists.
     */
    public Boolean exceptionDateExists(ReservationDateException resExcept, BindingResult result) {
        if (resExcept.getDatesOfReservationDateException().size() > 1) {
            for (int i = 0; i < resExcept.getDatesOfReservationDateException().size(); i++) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(resExcept.getDatesOfReservationDateException().get(i));
                if (getExceptionDates().contains(cal)) {
                    String msg = messageSource.getMessage("reservationDateException.oneOrMoredatesExists",
                            new Object[]{}, LocaleContextHolder.getLocale());
                    result.addError(new FieldError(result.getObjectName(), "endDate",
                            resExcept.getEndDate(), false,
                            null, null, msg));
                    return true;
                }
            }
        }
        else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(resExcept.getStartDate());
            if (getExceptionDates().contains(cal)) {
                String msg = messageSource.getMessage("reservationDateException.startDateExistst",
                        new Object[]{}, LocaleContextHolder.getLocale());
                result.addError(new FieldError(result.getObjectName(), "endDate",
                        null, false,
                        null, null, msg));
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the end date of the ReservationDateException is before the begin date.
     *
     * @param resExcept The ReservationDateException to check.
     * @param result    The BindingResult to hold any errors.
     * @return A boolean value representing whether the end date is before the begin date.
     */
    public Boolean isEndDateBeforeBeginDate(ReservationDateException resExcept, BindingResult result) {
        if (resExcept.getEndDate() != null) {
            if (resExcept.getEndDate().before(resExcept.getStartDate())) {
                String msg = messageSource.getMessage("reservationDateException.endDateBeforeBeginDate",
                        new Object[]{}, LocaleContextHolder.getLocale());
                result.addError(new FieldError(result.getObjectName(), "endDate",
                        null, false, null, null, msg));
                return true;
            }
        }
        return false;
    }
}
