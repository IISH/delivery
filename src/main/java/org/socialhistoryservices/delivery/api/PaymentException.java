package org.socialhistoryservices.delivery.api;

import com.mollie.mollie.utils.Response;

/**
 * Thrown when an invalid payment message was received from Mollie.
 */
public class PaymentException extends Exception {
    public PaymentException(Response response) {
        super("Received an invalid or unsuccessful request from Mollie with status code " + response.statusCode());
    }
}
