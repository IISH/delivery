package org.socialhistoryservices.delivery.request.service;

/**
 * Indicates one of the holdings specified is contained within a record which
 * is either INHERIT and a parent is CLOSED, or its own restrictionType is CLOSED.
 */
public class ClosedException extends Exception {
    public ClosedException() {
        super("One of the specified holdings is contained within a record " +
                "which is either INHERIT and a parent is CLOSED, or its own restrictionType is CLOSED.");
    }
}
