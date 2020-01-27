package org.socialhistoryservices.delivery.request.service;

/**
 * Indicates no holdings were specified.
 */
public class NoHoldingsException extends Exception {
    public NoHoldingsException() {
        super("No holdings specified. Must specify at least 1.");
    }
}
