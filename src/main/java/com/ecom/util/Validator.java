package com.ecom.util;

import java.util.regex.Pattern;

import com.ecom.exception.InvalidCustomerDataException;
import com.ecom.exception.InvalidPriceException;
import com.ecom.exception.InvalidQuantityException;


public class Validator {

	// Regex: standard email pattern
	private static final Pattern EMAIL_PATTERN = Pattern
			.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

	// Regex: exactly 10 digits
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{10}$");

	// Regex: at least 6 chars, 1 uppercase, 1 digit, 1 special char
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");

	public static void validateName(String name) {
		if (name == null || name.isBlank())
			throw new InvalidCustomerDataException("InvalidCustomerDataException: Name cannot be blank.");
	}

	public static void validateEmail(String email) {
		if (email == null || email.isBlank())
			throw new InvalidCustomerDataException("InvalidCustomerDataException: Email cannot be blank.");
		if (!EMAIL_PATTERN.matcher(email.trim()).matches())
			throw new InvalidCustomerDataException(
					"InvalidCustomerDataException: Invalid email format '" + email + "'. Expected: user@domain.com");
	}

	public static void validateMobile(String mobile) {
		if (mobile == null || mobile.isBlank())
			throw new InvalidCustomerDataException("InvalidCustomerDataException: Mobile number cannot be blank.");
		if (!MOBILE_PATTERN.matcher(mobile.trim()).matches())
			throw new InvalidCustomerDataException(
					"InvalidCustomerDataException: Mobile number must be exactly 10 digits (e.g. 9876543210). Got: '"
							+ mobile + "'");
	}

	
	public static void validateAddress(String address) {
		if (address == null || address.isBlank())
			throw new InvalidCustomerDataException("InvalidCustomerDataException: Address cannot be blank.");
	}

	
	public static void validatePassword(String password) {
		if (password == null || password.isBlank())
			throw new InvalidCustomerDataException("InvalidCustomerDataException: Password cannot be blank.");
		if (!PASSWORD_PATTERN.matcher(password).matches())
			throw new InvalidCustomerDataException(
					"InvalidCustomerDataException: Password must be at least 6 characters "
							+ "and contain at least 1 uppercase letter, 1 digit, and 1 special character (@#$%^&+=!).");
	}

	public static void validatePrice(double price) {
		if (price < 0)
			throw new InvalidPriceException(
					"InvalidPriceException: Price cannot be negative. Got: " + price);
	}

	public static void validateQuantity(int quantity) {
		if (quantity < 0)
			throw new InvalidQuantityException(
					"InvalidQuantityException: Quantity cannot be negative. Got: " + quantity);
	}
}
