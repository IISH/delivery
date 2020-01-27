package org.socialhistoryservices.delivery.record.service;

/**
 * Indicates the requested PID was detected as being a child record, but no parent of this child record is present.
 */
public class NoSuchParentException extends Exception {
    public NoSuchParentException() {
        super("No parent record found for provided child record. Please add parent first.");
    }
}
