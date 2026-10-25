package com.ecom.exception;

/**
 * Thrown when a product is not found inside a customer's cart.
 * Distinct from ProductNotFoundException which applies to the product catalog.
 */
public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}
