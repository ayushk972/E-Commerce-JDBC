package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.exception.*;
import com.ecom.model.Customers;
import com.ecom.repository.CustomersRepository;
import com.ecom.util.DataBaseConnection;

public class CustomersServiceTest {

    private CustomersRepository repository;
    private CustomersService service;
    private int customerId;
    private String email;

    @BeforeEach
    void setUp() {
        repository = new CustomersRepository();
        service = new CustomersService(repository);
        email = "cust_service_" + System.currentTimeMillis() + "@test.com";
        Customers registered = service.register("ServiceCust", email, "Pass@123", "9876543210", "123 Lane");
        customerId = registered.getCustomerId();
    }

    @AfterEach
    void tearDown() {
        if (customerId > 0) {
            try (Connection con = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                pstmt.setInt(1, customerId);
                pstmt.executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testRegisterValidation() {
        assertThrows(InvalidCustomerDataException.class, () ->
            service.register("", "a@b.com", "Pass@123", "9876543210", "addr"));
        assertThrows(InvalidCustomerDataException.class, () ->
            service.register("Name", "invalid-email", "Pass@123", "9876543210", "addr"));
        assertThrows(InvalidCustomerDataException.class, () ->
            service.register("Name", "a@b.com", "Pass@123", "123", "addr"));
        assertThrows(DuplicateEmailException.class, () ->
            service.register("Dup", email, "Pass@123", "9876543210", "addr"));
    }

    @Test
    void testLoginAndSearch() {
        Customers loggedIn = service.login(email, "Pass@123");
        assertNotNull(loggedIn);
        assertEquals(email, loggedIn.getEmail());

        assertThrows(InvalidCustomerDataException.class, () -> service.login(email, "WrongPass"));
        assertThrows(CustomerNotFoundException.class, () -> service.login("non_existent@email.com", "Pass@123"));
        assertThrows(InvalidCustomerDataException.class, () -> service.login("", "pass"));
        assertThrows(InvalidCustomerDataException.class, () -> service.login(email, ""));

        Customers found = service.searchById(customerId);
        assertNotNull(found);
        assertDoesNotThrow(() -> service.viewCustomer(customerId));
        assertDoesNotThrow(() -> service.viewAllCustomers());
        assertDoesNotThrow(() -> service.viewAllCustomersAdmin());
    }

    @Test
    void testUpdateAddressAndPassword() {
        assertThrows(InvalidCustomerDataException.class, () -> service.updateAddress(customerId, "   "));
        assertDoesNotThrow(() -> service.updateAddress(customerId, "789 New Road"));

        assertThrows(InvalidCustomerDataException.class, () -> service.updatePassword(customerId, "WrongOld", "NewPass@123"));
        assertThrows(InvalidCustomerDataException.class, () -> service.updatePassword(customerId, "Pass@123", "   "));
        assertDoesNotThrow(() -> service.updatePassword(customerId, "Pass@123", "BrandNewPass@123"));
    }
}
