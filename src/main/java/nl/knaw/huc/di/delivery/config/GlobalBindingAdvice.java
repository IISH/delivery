package nl.knaw.huc.di.delivery.config;

import nl.knaw.huc.di.delivery.record.dao.HoldingRepository;
import nl.knaw.huc.di.delivery.record.entity.ExternalRecordInfo;
import nl.knaw.huc.di.delivery.record.entity.Holding;
import nl.knaw.huc.di.delivery.reproduction.dao.ReproductionStandardOptionRepository;
import nl.knaw.huc.di.delivery.reproduction.entity.Reproduction;
import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionStandardOption;
import nl.knaw.huc.di.delivery.reservation.entity.Reservation;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.*;

@ControllerAdvice
public class GlobalBindingAdvice {

    private final SimpleDateFormat simpleDateFormat;
    private final HoldingRepository holdingRepository;
    private final ReproductionStandardOptionRepository reproductionStandardOptionRepository;

    public GlobalBindingAdvice(SimpleDateFormat simpleDateFormat, HoldingRepository holdingRepository, ReproductionStandardOptionRepository reproductionStandardOptionRepository) {
        this.simpleDateFormat = simpleDateFormat;
        this.holdingRepository = holdingRepository;
        this.reproductionStandardOptionRepository = reproductionStandardOptionRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(simpleDateFormat,
                true));
        binder.registerCustomEditor(String.class, new StringTrimmerEditor
                (true));

        binder.registerCustomEditor(Holding.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                Optional<Holding> oh = holdingRepository.findById(Integer.parseInt(text));
                oh.ifPresent(this::setValue);
            }
        });

        // This is needed for passing a reproduction standard option ID
        binder.registerCustomEditor(ReproductionStandardOption.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                int id = Integer.parseInt(text);
                Optional<ReproductionStandardOption> reproductionStandardOption =
                        reproductionStandardOptionRepository.findById(id);
                reproductionStandardOption.ifPresent(this::setValue);
            }
        });
    }

    /**
     * Usage Restriction type enumeration in Map format for use in views.
     *
     * @return The map with usage restriction types.
     */
    @ModelAttribute("usageRestriction_types")
    public Map<String, Holding.UsageRestriction> usageRestrictionTypes() {
        Map<String, Holding.UsageRestriction> data = new HashMap<>();
        data.put("OPEN", Holding.UsageRestriction.OPEN);
        data.put("CLOSED", Holding.UsageRestriction.CLOSED);
        return Map.copyOf(data);
    }


    /**
     * Map representation of status types of reservations for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("holding_status_types")
    public Map<String, Holding.Status> holdingStatusTypes() {
        Map<String, Holding.Status> data = new LinkedHashMap<>();
        data.put("AVAILABLE", Holding.Status.AVAILABLE);
        data.put("RESERVED", Holding.Status.RESERVED);
        data.put("IN_USE", Holding.Status.IN_USE);
        data.put("RETURNED", Holding.Status.RETURNED);
        return Map.copyOf(data);
    }

    /**
     * Map representation of status types of reproductions for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("reproduction_status_types")
    public Map<String, Reproduction.Status> reproductionStatusTypes() {
        Map<String, Reproduction.Status> data = new LinkedHashMap<>();
        data.put("WAITING_FOR_ORDER_DETAILS", Reproduction.Status.WAITING_FOR_ORDER_DETAILS);
        data.put("HAS_ORDER_DETAILS", Reproduction.Status.HAS_ORDER_DETAILS);
        data.put("CONFIRMED", Reproduction.Status.CONFIRMED);
        data.put("ACTIVE", Reproduction.Status.ACTIVE);
        data.put("COMPLETED", Reproduction.Status.COMPLETED);
        data.put("DELIVERED", Reproduction.Status.DELIVERED);
        data.put("CANCELLED", Reproduction.Status.CANCELLED);
        return Map.copyOf(data);
    }

    /**
     * Map representation of status types of reservations for use in views.
     *
     * @return The map {string status, enum status}.
     */
    @ModelAttribute("reservation_status_types")
    public Map<String, Reservation.Status> reservationstatusTypes() {
        Map<String, Reservation.Status> data = new LinkedHashMap<>();
        data.put("PENDING", Reservation.Status.PENDING);
        data.put("ACTIVE", Reservation.Status.ACTIVE);
        data.put("COMPLETED", Reservation.Status.COMPLETED);
        return Map.copyOf(data);
    }

    /**
     * Map representation of material types for use in views.
     *
     * @return The map {string material type, enum status}.
     */
    @ModelAttribute("material_types")
    public Map<String, ExternalRecordInfo.MaterialType> materialTypes() {
        Map<String, ExternalRecordInfo.MaterialType> data = new LinkedHashMap<>();
        data.put("SERIAL", ExternalRecordInfo.MaterialType.SERIAL);
        data.put("BOOK", ExternalRecordInfo.MaterialType.BOOK);
        data.put("SOUND", ExternalRecordInfo.MaterialType.SOUND);
        data.put("DOCUMENTATION", ExternalRecordInfo.MaterialType.DOCUMENTATION);
        data.put("ARCHIVE", ExternalRecordInfo.MaterialType.ARCHIVE);
        data.put("VISUAL", ExternalRecordInfo.MaterialType.VISUAL);
        data.put("MOVING_VISUAL", ExternalRecordInfo.MaterialType.MOVING_VISUAL);
        data.put("ARTICLE", ExternalRecordInfo.MaterialType.ARTICLE);
        data.put("OTHER", ExternalRecordInfo.MaterialType.OTHER);
        return Map.copyOf(data);
    }
    /**
     * Map representation of SOR level for use in views.
     *
     * @return The map.
     */
    @ModelAttribute("levels")
    public Map<String, ReproductionStandardOption.Level> levels() {
        Map<String, ReproductionStandardOption.Level> data = new LinkedHashMap<>();
        data.put("MASTER", ReproductionStandardOption.Level.MASTER);
        data.put("LEVEL1", ReproductionStandardOption.Level.LEVEL1);
        return Map.copyOf(data);
    }


}