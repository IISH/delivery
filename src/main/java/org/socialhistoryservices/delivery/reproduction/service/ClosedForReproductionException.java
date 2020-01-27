package org.socialhistoryservices.delivery.reproduction.service;

/**
 * Indicates one of the holdings specified is contained within a record of which the record is closed for reproduction.
 */
public class ClosedForReproductionException extends Exception {
    public ClosedForReproductionException() {
        super("One of the specified holdings is contained within a record " +
                "of which the record is closed for reproduction.");
    }
}
