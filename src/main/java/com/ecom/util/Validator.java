package com.ecom.util;

import java.util.regex.Pattern;

import com.ecom.exception.InvalidCustomerDataException;

/**
 * Centralised input validation utility.
 * All validation methods throw InvalidCustomerDataException on failure.
 */
public class Validator {

    // Regex: standard email pattern
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    // Regex: exactly 10 digits
    private static final Pattern MOBILE_PATTERN =
        Pattern.compile("^\\d{10}$");

    // Regex: at least 6 chars, 1 uppercase, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");

    /** Validates that a name is not blank. */
    public static void validateName(String name) {
        if (name == null || name.isBlank())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Name cannot be blank.");
    }

    /** Validates email format (must contain @ and a valid domain). */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Email cannot be blank.");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Invalid email format '" + email + "'. Expected: user@domain.com");
    }

    /** Validates mobile number — must be exactly 10 digits, no spaces or dashes. */
    public static void validateMobile(String mobile) {
        if (mobile == null || mobile.isBlank())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Mobile number cannot be blank.");
        if (!MOBILE_PATTERN.matcher(mobile.trim()).matches())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Mobile number must be exactly 10 digits (e.g. 9876543210). Got: '" + mobile + "'");
    }

    /** Validates address is not blank. */
    public static void validateAddress(String address) {
        if (address == null || address.isBlank())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Address cannot be blank.");
    }

    /**
     * Validates password strength:
     * - Min 6 characters
     * - At least 1 uppercase letter
     * - At least 1 digit
     * - At least 1 special character (@#$%^&+=!)
     */
    public static void validatePassword(String password) {
        if (password == null || password.isBlank())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Password cannot be blank.");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new InvalidCustomerDataException(
                "InvalidCustomerDataException: Password must be at least 6 characters " +
                "and contain at least 1 uppercase letter, 1 digit, and 1 special character (@#$%^&+=!).");
    }

    /** Validates a price value is not negative. */
    public static void validatePrice(double price) {
        if (price < 0)
            throw new com.ecom.exception.InvalidPriceException(
                "InvalidPriceException: Price cannot be negative. Got: " + price);
    }

    /** Validates a quantity value is not negative. */
    public static void validateQuantity(int quantity) {
        if (quantity < 0)
            throw new com.ecom.exception.InvalidQuantityException(
                "InvalidQuantityException: Quantity cannot be negative. Got: " + quantity);
    }
}
