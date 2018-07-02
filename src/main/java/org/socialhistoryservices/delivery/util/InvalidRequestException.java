package org.socialhistoryservices.delivery.util;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Represents an invalid request (HTTP status code 400).
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends RuntimeException {
    /**
     * Default constructor.
     */
    public InvalidRequestException() {
        super();
    }

    /**
     * Construct an invalid request exception.
     *
     * @param msg The message to set.
     */
    public InvalidRequestException(String msg) {
        super(msg);
    }

    /**
     * Create an invalid request exception providing the errors (for use with
     * forms).
     *
     * @param errors The errors to send.
     * @return The invalid request exception.
     */
    public static InvalidRequestException create(BindingResult errors) {
        StringBuilder msg = new StringBuilder();
        for (ObjectError err : errors.getAllErrors()) {
            if (err instanceof FieldError) {
                FieldError ferr = (FieldError) err;
                msg.append("'").append(ferr.getField()).append("':").append(ferr.getDefaultMessage()).append("\n");
            }
            else {
                msg.append("'").append(err.getObjectName()).append("':").append(err.getDefaultMessage()).append("\n");
            }

        }
        return new InvalidRequestException(msg.toString());
    }
}
