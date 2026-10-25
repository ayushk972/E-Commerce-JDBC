package com.ecom.exception;

/**
 * Thrown when a customer tries to cancel an order that has already been delivered.
 */
public class OrderAlreadyDeliveredException extends RuntimeException {
    public OrderAlreadyDeliveredException(String message) {
        super(message);
    }
}
