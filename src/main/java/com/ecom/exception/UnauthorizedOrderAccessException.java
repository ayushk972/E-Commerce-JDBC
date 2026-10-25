package com.ecom.exception;

/**
 * Thrown when a customer tries to access or cancel an order that does not belong to them.
 */
public class UnauthorizedOrderAccessException extends RuntimeException {
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
