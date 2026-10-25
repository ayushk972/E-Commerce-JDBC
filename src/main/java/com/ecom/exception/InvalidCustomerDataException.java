package com.ecom.exception;

/**
 * Thrown when customer registration or update data is invalid —
 * e.g., blank name, invalid email format, or empty address/mobile.
 */
public class InvalidCustomerDataException extends RuntimeException {
    public InvalidCustomerDataException(String message) {
        super(message);
    }
}
