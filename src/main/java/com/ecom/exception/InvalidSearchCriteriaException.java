package com.ecom.exception;

/**
 * Thrown when a search is performed with invalid criteria,
 * e.g., min price greater than max price, or a blank search term.
 */
public class InvalidSearchCriteriaException extends RuntimeException {
    public InvalidSearchCriteriaException(String message) {
        super(message);
    }
}
