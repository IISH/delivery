package nl.knaw.huc.di.delivery.reservation.service;

import nl.knaw.huc.di.delivery.request.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import nl.knaw.huc.di.delivery.config.PrinterConfiguration;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.entity.Record;
import nl.knaw.huc.di.delivery.record.service.RecordService;
import nl.knaw.huc.di.delivery.request.entity.HoldingRequest;
import nl.knaw.huc.di.delivery.request.entity.Request;
import nl.knaw.huc.di.delivery.request.service.*;
import nl.knaw.huc.di.delivery.reservation.dao.HoldingReservationDAO;
import nl.knaw.huc.di.delivery.reservation.dao.ReservationDAO;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.springframework.validation.Validator;
import java.awt.print.PrinterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents the service of the reservation package.
 */
@Service
public class ReservationServiceImpl extends AbstractRequestService implements ReservationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationDAO reservationDAO;
    private final HoldingReservationDAO holdingReservationDAO;
    private final ReservationDateExceptionService dateExceptionService;
    private final PrinterConfiguration printerConfiguration;
    private final MessageSource messageSource;
    private final RecordService recordService;
    private final DeliveryProperties deliveryProperties;
    private final SimpleDateFormat simpleDateFormat;

    public ReservationServiceImpl(ReservationDAO reservationDAO, HoldingReservationDAO holdingReservationDAO, ReservationDateExceptionService dateExceptionService, PrinterConfiguration printerConfiguration, Validator mvcValidator, MessageSource messageSource, GeneralRequestService generalRequestService, RecordService recordService, DeliveryProperties deliveryProperties, SimpleDateFormat simpleDateFormat) {
        super(mvcValidator, messageSource, generalRequestService, recordService, deliveryProperties);
        this.reservationDAO = reservationDAO;
        this.holdingReservationDAO = holdingReservationDAO;
        this.dateExceptionService = dateExceptionService;
        this.printerConfiguration = printerConfiguration;
        this.messageSource = messageSource;
        this.recordService = recordService;
        this.deliveryProperties = deliveryProperties;
        this.simpleDateFormat = simpleDateFormat;
    }

    /**
     * Add a Reservation to the database.
     *
     * @param obj Reservation to add.
     */
    public void addReservation(Reservation obj) {
        // Make sure the holdings get set to the correct status.
        updateStatusAndAssociatedHoldingStatus(obj, obj.getStatus());

        // Add to the database
        reservationDAO.add(obj);
    }

    /**
     * Remove a Reservation from the database.
     *
     * @param obj Reservation to remove.
     */
    public void removeReservation(Reservation obj) {
        // Set all holdings linked  to this reservation back to AVAILABLE if
        // this is a PENDING/ACTIVE reservation.
        // Note that we are in a transaction here, so it does not matter the
        // records are still linked to the reservation when setting them to available.
        if (obj.getStatus() != Reservation.Status.COMPLETED) {
            changeHoldingStatus(obj, Holding.Status.AVAILABLE);
        }
        reservationDAO.remove(obj);
    }

    /**
     * Change the status of all holdings in a reservation.
     *
     * @param res    Reservation to change status for.
     * @param status Status to change holdings to.
     */
    public void changeHoldingStatus(Reservation res, Holding.Status status) {
        super.changeHoldingStatus(res, status);
        updateHoldingStatusForPendingReservations(res);
        saveReservation(res);
    }

    /**
     * Save changes to a Reservation in the database.
     *
     * @param obj Reservation to save.
     */
    public Reservation saveReservation(Reservation obj) {
        return reservationDAO.save(obj);
    }

    /**
     * Retrieve the Reservation matching the given Id.
     *
     * @param id Id of the Reservation to retrieve.
     * @return The Reservation matching the Id.
     */
    public Reservation getReservationById(int id) {
        return reservationDAO.getById(id);
    }

    /**
     * Get a criteria builder for querying Reservations.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getReservationCriteriaBuilder() {
        return reservationDAO.getCriteriaBuilder();
    }

    /**
     * Get a criteria builder for querying HoldingReservations.
     *
     * @return the CriteriaBuilder.
     */
    public CriteriaBuilder getHoldingReservationCriteriaBuilder() {
        return holdingReservationDAO.getCriteriaBuilder();
    }

    /**
     * List all Reservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Reservations.
     */
    public List<Reservation> listReservations(CriteriaQuery<Reservation> q) {
        return reservationDAO.list(q);
    }

    /**
     * List all Tuples matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching Tuples.
     */
    public List<Tuple> listTuples(CriteriaQuery<Tuple> q) {
        return reservationDAO.listForTuple(q);
    }

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> listHoldingReservations(CriteriaQuery<HoldingReservation> q) {
        return holdingReservationDAO.list(q);
    }

    /**
     * List all HoldingReservations matching a built query.
     *
     * @param q           The criteria query to execute
     * @param firstResult The first result to obtain
     * @param maxResults  The max number of results to obtain
     * @return A list of matching HoldingReservations.
     */
    public List<HoldingReservation> listHoldingReservations(CriteriaQuery<HoldingReservation> q,
                                                            int firstResult, int maxResults) {
        return holdingReservationDAO.list(q, firstResult, maxResults);
    }

    /**
     * Count all HoldingReservations matching a built query.
     *
     * @param q The criteria query to execute
     * @return A count of matching HoldingReservations.
     */
    public long countHoldingReservations(CriteriaQuery<Long> q) {
        return holdingReservationDAO.count(q);
    }

    /**
     * Get a single Reservation matching a built query.
     *
     * @param query The query to match by.
     * @return The matching Reservation.
     */
    public Reservation getReservation(CriteriaQuery<Reservation> query) {
        return reservationDAO.get(query);
    }

    /**
     * Mark a specific item in a reservation as seen, bumping it to the next status.
     *
     * @param res Reservation to change status for.
     * @param h   Holding to bump.
     */
    public void markItem(Reservation res, Holding h) {
        // Ignore old reservations
        if (res == null)
            return;

        Holding.Status status = super.markItem(h);
        LOGGER.info("Status set to {}", status.name());

        markReservation(res);
        updateHoldingStatusForPendingReservations(res);
        saveReservation(res);
    }

    /**
     * Mark a reservation, bumping it to the next status.
     *
     * @param res Reservation to change status for.
     */
    private void markReservation(Reservation res) {
        boolean complete = true;
        boolean reserved = true;
        for (HoldingReservation hr : res.getHoldingReservations()) {
            if (!hr.isCompleted()) {
                Holding holding = hr.getHolding();

                if (holding.getStatus() == Holding.Status.AVAILABLE) {
                    hr.setCompleted(true);
                } else {
                    complete = false;
                }

                if (holding.getStatus() != Holding.Status.RESERVED) {
                    reserved = false;
                }
            }
        }

        if (complete) {
            res.setStatus(Reservation.Status.COMPLETED);
        } else if (reserved) {
            res.setStatus(Reservation.Status.PENDING);
        } else {
            res.setStatus(Reservation.Status.ACTIVE);
        }
    }

    /**
     * Merge the other reservation's fields into this reservation.
     *
     * @param reservation The reservation.
     * @param other       The other reservation to merge with.
     */
    public void merge(Reservation reservation, Reservation other) {
        reservation.setDate(other.getDate());
        reservation.setReturnDate(other.getReturnDate());
        reservation.setVisitorName(other.getVisitorName());
        reservation.setVisitorEmail(other.getVisitorEmail());
        reservation.setComment(other.getComment());

        if (other.getHoldingReservations() == null) {
            for (HoldingReservation hr : reservation.getHoldingReservations()) {
                recordService.updateHoldingStatus(hr.getHolding(), Holding.Status.AVAILABLE);
            }
            reservation.setHoldingReservations(new ArrayList<>());
        } else {
            // Delete holdings that were not provided.
            deleteHoldingsNotInProvidedRequest(reservation, other);

            // Add/update provided.
            addOrUpdateHoldingsProvidedByRequest(reservation, other);
        }

        updateStatusAndAssociatedHoldingStatus(reservation, other.getStatus());
    }

    /**
     * Set the reservation status and update the associated holdings status accordingly. Only updates status forward.
     *
     * @param reservation The reservation.
     * @param status      The reservation which changed status.
     */
    public void updateStatusAndAssociatedHoldingStatus(Reservation reservation, Reservation.Status status) {
        if (status.ordinal() < reservation.getStatus().ordinal()) {
            return;
        }

        reservation.setStatus(status);

        Holding.Status hStatus = switch (status) {
            case PENDING -> Holding.Status.RESERVED;
            case ACTIVE -> Holding.Status.IN_USE;
            default -> Holding.Status.AVAILABLE;
        };

        for (HoldingReservation hr : reservation.getHoldingReservations()) {
            if (!hr.isCompleted()) {
                if (status == Reservation.Status.COMPLETED)
                    hr.setCompleted(true);

                recordService.updateHoldingStatus(hr.getHolding(), hStatus);
            }
        }

        updateHoldingStatusForPendingReservations(reservation);
    }

    /**
     * Update the holding status for pending reservations.
     *
     * @param reservation The reservation to check.
     */
    private void updateHoldingStatusForPendingReservations(Reservation reservation) {
        if (reservation.getStatus() != Reservation.Status.COMPLETED) {
            return;
        }

        for (HoldingReservation hr : reservation.getHoldingReservations()) {
            Holding holding = hr.getHolding();
            if (holding.getStatus() == Holding.Status.AVAILABLE) {
                Record record = holding.getRecord();
                if (record.getParent() != null && record.getExternalInfo().getContainer() != null
                        && reservationDAO.hasPendingReservation(record)) {
                    recordService.updateHoldingStatus(holding, Holding.Status.RESERVED);
                }
            }
        }
    }

    /**
     * Adds a HoldingRequest to the HoldingRequests assoicated with this request.
     *
     * @param holdingRequest The HoldingRequests to add.
     */
    protected void addToHoldingRequests(Request request, HoldingRequest holdingRequest) {
        Reservation reservation = (Reservation) request;
        HoldingReservation holdingReservation = (HoldingReservation) holdingRequest;

        holdingReservation.setReservation(reservation);
        reservation.getHoldingReservations().add(holdingReservation);
    }

    /**
     * Prints holding reservations by using the default printer.
     *
     * @param hrs         The holding reservations to print.
     * @param alwaysPrint If set to true, already printed holdings will also be printed.
     * @throws PrinterException Thrown when delivering the print job to the
     *                          printer failed. Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    public void printItems(List<HoldingReservation> hrs, boolean alwaysPrint) throws PrinterException {
        try {
            Set<Reservation> reservations = new HashSet<>();
            List<RequestPrintable> requestPrintables = new ArrayList<>();
            List<RequestPrintable> requestPrintablesArchive = new ArrayList<>();

            for (HoldingReservation hr : hrs) {
                ExternalRecordInfo.MaterialType mt = hr.getHolding().getRecord().getExternalInfo().getMaterialType();
                if (mt == ExternalRecordInfo.MaterialType.ARCHIVE) {
                    requestPrintablesArchive.add(new ArchiveReservationPrintable(
                            hr, messageSource, simpleDateFormat, deliveryProperties));
                } else {
                    requestPrintables.add(new ReservationPrintable(
                            hr, messageSource, simpleDateFormat, deliveryProperties));
                }
                reservations.add(hr.getReservation());
            }

            printRequest(requestPrintables, printerConfiguration.getPrinterNameArchive(), alwaysPrint);
            printRequest(requestPrintablesArchive, printerConfiguration.getPrinterNameReadingRoom(), alwaysPrint);

            for (Reservation r : reservations) {
                saveReservation(r);
            }
        } catch (PrinterException e) {
            LOGGER.warn("Printing reservation failed", e);
            throw e;
        }
    }

    /**
     * Print a reservation if it was not printed yet.
     *
     * @param res The reservation to print.
     * @throws PrinterException Thrown when delivering the print job to the
     *                          printer failed. Does not say anything if the printer actually printed
     *                          (or ran out of paper for example).
     */
    public void printReservation(Reservation res) throws PrinterException {
        printItems(res.getHoldingReservations(), false);
    }

    /**
     * Edit reservations.
     *
     * @param newRes The new reservation to put in the database.
     * @param oldRes The old reservation in the database (if present).
     * @param result The binding result object to put the validation errors in.
     * @throws ClosedException     Thrown when a holding is provided which
     *                             references a record which is restrictionType=CLOSED.
     * @throws NoHoldingsException Thrown when no holdings are provided.
     */
    public void createOrEdit(Reservation newRes, Reservation oldRes, BindingResult result)
            throws ClosedException, NoHoldingsException {
        // Validate the reservation.
        validateRequest(newRes, result);

        // Make sure a valid reservation date is provided (Only upon creation
        // because time dependent!).
        if (oldRes == null) {
            Date resDate = newRes.getDate();
            if (resDate != null && !resDate.equals(getFirstValidReservationDate(resDate))) {
                Calendar resCalendar = Calendar.getInstance();
                resCalendar.setTime(resDate);

                String msg;
                if (dateExceptionService.getExceptionDates().contains(resCalendar)) {
                    msg = messageSource.getMessage("reservationDateException.dateIsException",
                            new Object[]{}, LocaleContextHolder.getLocale()) +
                            dateExceptionService.getReasonForExceptionDate(resCalendar);
                } else {
                    msg = messageSource.getMessage("validator.reservationDate", null,
                            "Invalid date", LocaleContextHolder.getLocale());
                }

                result.addError(new FieldError(result.getObjectName(), "date",
                        newRes.getDate(), false, null, null, msg));
            }
        }

        // If the return date is provided, it should be >= to the date of visit.
        Date d = newRes.getDate();
        Date rd = newRes.getReturnDate();
        if (d != null && rd != null && rd.before(d)) {
            String msg = messageSource.getMessage("validator.reservationReturnDate", null,
                    "Invalid date", LocaleContextHolder.getLocale());
            result.addError(new FieldError(result.getObjectName(), "returnDate",
                    newRes.getDate(), false, null, null, msg));
        }

        // Execute this method below the date check, or else the date will
        // not be checked if this method throws an exception; not displaying
        // the error immediately, but only when the holdings are valid instead.
        validateHoldings(newRes, oldRes);

        // Add or save the record when no errors are present.
        if (!result.hasErrors()) {
            if (oldRes == null) {
                newRes.setCreationDate(new Date());
                addReservation(newRes);
            } else {
                merge(oldRes, newRes);
                saveReservation(oldRes);
            }
        }
    }

    /**
     * Get the first valid reservation date after or equal to from.
     *
     * @param from The date to start from.
     * @return The first valid date, or null when maxDaysInAdvance was exceeded.
     */
    public Date getFirstValidReservationDate(Date from) {
        Calendar fromCal = GregorianCalendar.getInstance();
        fromCal.setTime(from);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setLenient(false);

        Calendar t = GregorianCalendar.getInstance();
        try {
            t.setTime(format.parse(deliveryProperties.getRequestLatestTime()));
        } catch (ParseException e) {
            throw new RuntimeException("Invalid reservationLatestTime provided in config. Should be of format HH:mm");
        }

        Calendar firstPossibleCal = GregorianCalendar.getInstance();

        // Cannot reserve after "closing" time.
        if (firstPossibleCal.get(Calendar.HOUR_OF_DAY) > t.get(Calendar.HOUR_OF_DAY) ||
                (firstPossibleCal.get(Calendar.HOUR_OF_DAY) == t.get(Calendar.HOUR_OF_DAY) &&
                        firstPossibleCal.get(Calendar.MINUTE) >= t.get(Calendar.MINUTE))) {
            firstPossibleCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Cannot reserve in past (or after closing time).
        if (fromCal.get(Calendar.YEAR) < firstPossibleCal.get(Calendar.YEAR)
                || (fromCal.get(Calendar.YEAR) == firstPossibleCal.get(Calendar.YEAR)
                && fromCal.get(Calendar.DAY_OF_YEAR) < firstPossibleCal.get(Calendar.DAY_OF_YEAR))) {
            fromCal = firstPossibleCal;
        }

        // Check if date is an exception date
        // Vrijdag zaterdag en zondag geen diensten.
        List<Calendar> exceptionDates = dateExceptionService.getExceptionDates();
        for (Calendar exceptionDate : exceptionDates) {
            if (fromCal.equals(exceptionDate)) {
                fromCal.add(Calendar.DAY_OF_YEAR, 1);
                // Check for weekends
                if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    fromCal.add(Calendar.DAY_OF_YEAR, 3);
                }
                if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                    fromCal.add(Calendar.DAY_OF_YEAR, 2);
                }
                if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    fromCal.add(Calendar.DAY_OF_YEAR, 1);
                }
            }
        }

        // Vrijdag zaterdag en zondag geen diensten.
        if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            fromCal.add(Calendar.DAY_OF_YEAR, 3);
        }
        if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            fromCal.add(Calendar.DAY_OF_YEAR, 2);
        }
        if (fromCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            fromCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Calendar maxCal = GregorianCalendar.getInstance();
        int maxDaysInAdvance = deliveryProperties.getReservationMaxDaysInAdvance();
        maxCal.add(Calendar.DAY_OF_YEAR, maxDaysInAdvance);
        if (fromCal.get(Calendar.YEAR) > maxCal.get(Calendar.YEAR)
                || (fromCal.get(Calendar.YEAR) == maxCal.get(Calendar.YEAR)
                && fromCal.get(Calendar.DAY_OF_YEAR) > maxCal.get(Calendar.DAY_OF_YEAR))) {
            return null;
        }

        return fromCal.getTime();
    }

    /**
     * Returns the active reservation with which this holding is associated.
     *
     * @param holding The Holding to get the active reservation of.
     * @return The active reservation, or null if no active reservation exists.
     */
    public Reservation getActiveFor(Holding holding) {
        return reservationDAO.getActiveFor(holding);
    }

    @Override
    public Page<HoldingReservation> findAll(CriteriaQuery<HoldingReservation> cq, Pageable pageable) {
        return holdingReservationDAO.findAll(cq, pageable);
    }
}
