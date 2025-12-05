package org.socialhistoryservices.delivery.reproduction.service;

/**
 * Indicates that we failed to register the order in Mollie.
 */
public class OrderRegistrationFailureException extends Exception {
    public OrderRegistrationFailureException(Throwable cause) {
        super("Failed to register the order in Mollie.", cause);
    }
}
