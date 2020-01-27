package org.socialhistoryservices.delivery.reproduction.service;

/**
 * Indicates one of the holdings specified requires a custom reproduction
 * of which no price an/ord delivery time is known yet.
 */
public class IncompleteOrderDetailsException extends Exception {
    public IncompleteOrderDetailsException() {
        super("One of the holdings specified requires a custom reproduction" +
                " of which no price and/or delivery time is known yet.");
    }
}
