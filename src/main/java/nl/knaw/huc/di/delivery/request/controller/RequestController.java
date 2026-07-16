package nl.knaw.huc.di.delivery.request.controller;

import nl.knaw.huc.di.delivery.config.DeliveryProperties;
import nl.knaw.huc.di.delivery.record.dao.HoldingDAO;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.record.service.RecordService;
import nl.knaw.huc.di.delivery.reproduction.dao.HoldingReproductionDAO;
import nl.knaw.huc.di.delivery.reproduction.entity.HoldingReproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.reproduction.service.ReproductionService;
import nl.knaw.huc.di.delivery.request.entity.Request;
import nl.knaw.huc.di.delivery.request.service.GeneralRequestService;
import nl.knaw.huc.di.delivery.reservation.dao.HoldingReservationDAO;
import nl.knaw.huc.di.delivery.reservation.entity.HoldingReservation;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import nl.knaw.huc.di.delivery.reservation.service.ReservationService;
import nl.knaw.huc.di.delivery.services.UrlDecoderService;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

/**
 * Controller of the Request package, handles all /request/* requests.
 */
@Controller
@RequestMapping(value = "/request")
public class RequestController extends AbstractRequestController {
    private final ReservationService reservations;
    private final ReproductionService reproductions;
    private final GeneralRequestService generalRequestService;
    private final HoldingReservationDAO holdingReservationDAO;
    private final HoldingReproductionDAO holdingReproductionDAO;
    private final HoldingDAO holdingDAO;

    public RequestController(MessageSource messageSource, ReservationService reservations, ReproductionService reproductions, GeneralRequestService generalRequestService, HoldingReservationDAO holdingReservationDAO, HoldingReproductionDAO holdingReproductionDAO, HoldingDAO holdingDAO, RecordService recordService, DeliveryProperties deliveryProperties, UrlDecoderService urlDecoderService) {
        super(deliveryProperties, messageSource, generalRequestService, recordService, urlDecoderService);
        this.reservations = reservations;
        this.reproductions = reproductions;
        this.generalRequestService = generalRequestService;
        this.holdingReservationDAO = holdingReservationDAO;
        this.holdingReproductionDAO = holdingReproductionDAO;
        this.holdingDAO = holdingDAO;
    }

    /**
     * Get the barcode scan page.
     *
     * @return The view to resolve.
     */
    @RequestMapping(value = "/scan", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ROLE_RESERVATION_MODIFY', 'ROLE_REPRODUCTION_MODIFY')")
    public String scanBarcode() {
        return "request_scan";
    }

    /**
     * Process a scanned barcode.
     *
     * @param id    The scanned Record id.
     * @param model The model to add response attributes to.
     * @return The view to resolve.
     */
    @RequestMapping(value = "/scan", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ROLE_RESERVATION_MODIFY', 'ROLE_REPRODUCTION_MODIFY')")
    public String scanBarcode(@RequestParam(required = false) String id, Model model) {
        // Obtain the scanned holding
        Holding h;
        try {
            int ID = Integer.parseInt(id);
            HoldingReservation holdingReservation = holdingReservationDAO.getById(ID);
            HoldingReproduction holdingReproduction = holdingReproductionDAO.getById(ID);
            Holding h2 = holdingDAO.getById(ID);
            if (holdingReproduction != null && !holdingReproduction.isCompleted())
                h = holdingReproduction.getHolding();
            else if (holdingReservation != null && !holdingReservation.isCompleted())
                h = holdingReservation.getHolding();
            else
                h = h2;
        } catch (NumberFormatException ex) {
            h = null;
        }

        if (h == null) {
            model.addAttribute("error", "invalid");
            return "request_scan";
        }

        // Information about the current state
        Holding.Status oldStatus = h.getStatus();
        Request requestActive = generalRequestService.getActiveFor(h);

        // Determine the active request
        Reservation reservation = null;
        Reproduction reproduction = null;
        if (requestActive instanceof Reservation)
            reservation = (Reservation) requestActive;
        if (requestActive instanceof Reproduction)
            reproduction = (Reproduction) requestActive;

        // Show the request corresponding to the scanned record
        if ((reservation != null) || (reproduction != null)) {
            // If the user may modify reservations, mark item for the active reservation
            if (reservation != null)
                reservations.markItem(reservation, h);

            // If the user may modify reproductions, mark item for the active reproduction
            if (reproduction != null)
                reproductions.markItem(reproduction, h);

            model.addAttribute("holding", h);
            model.addAttribute("oldStatus", oldStatus);

            model.addAttribute("reservation", reservation);
            model.addAttribute("reproduction", reproduction);

            model.addAttribute("requestActive", getRequestAsString(requestActive));

            // Also add information about the state of each of the reservation and/or reproduction holdings
            Set<Holding> holdings = new HashSet<>();
            if ((reservation != null) && (reservation.getHoldings() != null))
                holdings.addAll(reservation.getHoldings());
            if ((reproduction != null) && (reproduction.getHoldings() != null))
                holdings.addAll(reproduction.getHoldings());
            model.addAttribute("holdingActiveRequests", getHoldingActiveRequests(holdings));

            return "request_scan";
        }

        model.addAttribute("error", "invalid");
        return "request_scan";
    }
}
