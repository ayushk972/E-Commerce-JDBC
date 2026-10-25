package com.ecom.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.ecom.exception.*;

public class ValidatorTest {

    @Test
    void testValidateName() {
        assertDoesNotThrow(() -> Validator.validateName("John Doe"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateName(null));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateName("   "));
    }

    @Test
    void testValidateEmail() {
        assertDoesNotThrow(() -> Validator.validateEmail("user@example.com"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateEmail(null));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateEmail(""));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateEmail("invalid-email"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateEmail("user@com"));
    }

    @Test
    void testValidateMobile() {
        assertDoesNotThrow(() -> Validator.validateMobile("9876543210"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateMobile(null));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateMobile(""));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateMobile("12345"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateMobile("98765432101"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateMobile("98765abcde"));
    }

    @Test
    void testValidateAddress() {
        assertDoesNotThrow(() -> Validator.validateAddress("123 MG Road"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateAddress(null));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validateAddress("   "));
    }

    @Test
    void testValidatePassword() {
        assertDoesNotThrow(() -> Validator.validatePassword("Secret@123"));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword(null));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword(""));
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword("Pass1")); // < 6 chars
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword("secret@123")); // no uppercase
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword("Secret!ABC")); // no digit
        assertThrows(InvalidCustomerDataException.class, () -> Validator.validatePassword("Secret1234")); // no special char
    }

    @Test
    void testValidatePrice() {
        assertDoesNotThrow(() -> Validator.validatePrice(0.0));
        assertDoesNotThrow(() -> Validator.validatePrice(199.99));
        assertThrows(InvalidPriceException.class, () -> Validator.validatePrice(-0.01));
        assertThrows(InvalidPriceException.class, () -> Validator.validatePrice(-100.0));
    }

    @Test
    void testValidateQuantity() {
        assertDoesNotThrow(() -> Validator.validateQuantity(0));
        assertDoesNotThrow(() -> Validator.validateQuantity(50));
        assertThrows(InvalidQuantityException.class, () -> Validator.validateQuantity(-1));
    }
}
