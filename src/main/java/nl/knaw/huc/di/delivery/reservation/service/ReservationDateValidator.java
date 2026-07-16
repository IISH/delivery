package nl.knaw.huc.di.delivery.reservation.service;

import org.springframework.stereotype.Component;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Date;

/**
 * Validates whether a reservation date is allowable.
 */
@Component
public class ReservationDateValidator implements ConstraintValidator<ValidReservationDate, Date> {
    private final ReservationService reservations;

    public ReservationDateValidator(ReservationService reservations) {
        this.reservations = reservations;
    }

    /**
     * Initialize this validator.
     *
     * @param annotation The annotation to use.
     */
    public void initialize(ValidReservationDate annotation) {
    }

    /**
     * Check whether a date is valid or not.
     *
     * @param dt  The object to be considered as a date.
     * @param ctx The context.
     * @return Whether the date is valid or not.
     */
    public boolean isValid(Date dt, ConstraintValidatorContext ctx) {
        // Nulls are considered invalid
        if (dt == null)
            return false;

        Date valid = reservations.getFirstValidReservationDate(dt);

        return valid != null && valid.equals(dt);
    }
}
