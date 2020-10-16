package org.socialhistoryservices.delivery.api;

/**
 * Indicates the requested PID does not exist in Evergreen/EAD.
 */
public class NoSuchPidException extends Exception {
    public NoSuchPidException() {
        super("No record with provided PID found in lookup service");
    }
}
