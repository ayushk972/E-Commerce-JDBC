package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ecom.exception.*;
import com.ecom.model.Customers;
import com.ecom.repository.CustomersRepository;

public class CustomersServiceTest {

    @Mock
    private CustomersRepository customerRepository;

    @InjectMocks
    private CustomersService customersService;

    private Customers sampleCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleCustomer = new Customers(101, "Alice", "alice@example.com", "Secret@123", "9876543210", "123 Main St");
    }

    @Test
    void testRegisterSuccess() {
        when(customerRepository.emailExists("alice@example.com")).thenReturn(false);
        when(customerRepository.addCustomer(any(Customers.class))).thenReturn(101);
        when(customerRepository.findById(101)).thenReturn(sampleCustomer);

        Customers result = customersService.register("Alice", "alice@example.com", "Secret@123", "9876543210", "123 Main St");
        assertNotNull(result);
        assertEquals(101, result.getCustomerId());
    }

    @Test
    void testRegisterDuplicateEmail() {
        when(customerRepository.emailExists("alice@example.com")).thenReturn(true);
        assertThrows(DuplicateEmailException.class,
            () -> customersService.register("Alice", "alice@example.com", "Secret@123", "9876543210", "123 Main St"));
    }

    @Test
    void testLoginSuccess() {
        when(customerRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleCustomer));
        Customers c = customersService.login("alice@example.com", "Secret@123");
        assertEquals("Alice", c.getName());
    }

    @Test
    void testLoginFailures() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        when(customerRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleCustomer));

        assertThrows(CustomerNotFoundException.class, () -> customersService.login("unknown@example.com", "Pass@123"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.login("alice@example.com", "WrongPass@123"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.login("", "Pass@123"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.login("alice@example.com", ""));
    }

    @Test
    void testViewAndSearch() {
        when(customerRepository.findById(101)).thenReturn(sampleCustomer);
        when(customerRepository.getAllCustomers()).thenReturn(List.of(sampleCustomer));

        assertDoesNotThrow(() -> customersService.viewCustomer(101));
        assertEquals(sampleCustomer, customersService.searchById(101));
        assertDoesNotThrow(() -> customersService.viewAllCustomers());
        assertDoesNotThrow(() -> customersService.viewAllCustomersAdmin());
    }

    @Test
    void testUpdateAddress() {
        when(customerRepository.findById(101)).thenReturn(sampleCustomer);
        assertDoesNotThrow(() -> customersService.updateAddress(101, "New Address 123"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.updateAddress(101, "   "));
    }

    @Test
    void testUpdatePassword() {
        when(customerRepository.findById(101)).thenReturn(sampleCustomer);
        assertDoesNotThrow(() -> customersService.updatePassword(101, "Secret@123", "NewSecret@456"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.updatePassword(101, "WrongOld@123", "NewSecret@456"));
        assertThrows(InvalidCustomerDataException.class, () -> customersService.updatePassword(101, "Secret@123", "short"));
    }
}
