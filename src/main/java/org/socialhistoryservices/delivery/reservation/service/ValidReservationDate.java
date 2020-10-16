package org.socialhistoryservices.delivery.reservation.service;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Date validation annotation.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ReservationDateValidator.class)
@Documented
public @interface ValidReservationDate {
    /**
     * The message to display when invalidated.
     */
    String message() default "{mvcValidator.reservationDate}";

    /**
     * The group this annotation belongs to.
     */
    Class<?>[] groups() default {};

    /**
     * The annotation data.
     */
    Class<? extends Payload>[] payload() default {};
}
