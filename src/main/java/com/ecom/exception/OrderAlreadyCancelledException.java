package com.ecom.exception;

/**
 * Thrown when an operation is attempted on an order that has already been cancelled.
 */
public class OrderAlreadyCancelledException extends RuntimeException {
    public OrderAlreadyCancelledException(String message) {
        super(message);
    }
}
