package com.ecom.exception;

/**
 * Thrown when a customer tries to register with an email that is already in use.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
