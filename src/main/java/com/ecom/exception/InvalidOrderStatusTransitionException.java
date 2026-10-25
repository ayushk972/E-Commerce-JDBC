package com.ecom.exception;

/**
 * Thrown when an admin tries to move an order's status in an invalid direction
 * (e.g., backwards from SHIPPED to CONFIRMED, or updating a CANCELLED order).
 */
public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}
