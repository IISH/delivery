package org.socialhistoryservices.delivery.reproduction.service;

/**
 * Indicates one of the holdings specified requires a custom reproduction
 * of which no price and delivery time is known yet.
 */
public class OrderNotReadyException extends Exception {
    public OrderNotReadyException() {
        super("One of the holdings specified requires a custom reproduction" +
                " of which no price and delivery time is known yet.");
    }
}
