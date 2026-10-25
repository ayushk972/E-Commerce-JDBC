package com.ecom.exception;

/**
 * Thrown when a customer tries to place an order but their cart is empty.
 */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}
