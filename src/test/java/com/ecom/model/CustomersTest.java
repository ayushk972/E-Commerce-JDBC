package com.ecom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomersTest {

    private Customers customer;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        customer = new Customers(101, "Alice", "alice@example.com", "Secret@123", "9876543210", "123 Main St", now, now);
    }

    @Test
    void testGuestConstructor() {
        Customers guest = new Customers();
        assertEquals(0, guest.getCustomerId());
        assertEquals("Guest", guest.getName());
        assertNotNull(guest.getCreatedAt());
        assertNotNull(guest.getUpdatedAt());
    }

    @Test
    void testStandardConstructor() {
        Customers c = new Customers(102, "Bob", "bob@example.com", "Pass@123", "9876543211", "456 Elm St");
        assertEquals(102, c.getCustomerId());
        assertEquals("Bob", c.getName());
        assertEquals("bob@example.com", c.getEmail());
        assertEquals("Pass@123", c.getPassword());
        assertEquals("9876543211", c.getMobile());
        assertEquals("456 Elm St", c.getAddress());
        assertNotNull(c.getCreatedAt());
        assertNotNull(c.getUpdatedAt());
    }

    @Test
    void testSetters() {
        customer.setAddress("789 New St");
        customer.setPassword("NewPass@123");
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        LocalDateTime updated = LocalDateTime.now().plusDays(2);
        customer.setCreatedAt(created);
        customer.setUpdatedAt(updated);

        assertEquals("789 New St", customer.getAddress());
        assertEquals("NewPass@123", customer.getPassword());
        assertEquals(created, customer.getCreatedAt());
        assertEquals(updated, customer.getUpdatedAt());
    }

    @Test
    void testToStringCustomerView() {
        String str = customer.toString();
        assertTrue(str.contains("Alice"));
        assertTrue(str.contains("alice@example.com"));
        assertTrue(str.contains("9876543210"));
        assertFalse(str.contains("Created At"));
        assertFalse(str.contains("Updated At"));
    }

    @Test
    void testToAdminString() {
        String adminStr = customer.toAdminString();
        assertTrue(adminStr.contains("Alice"));
        assertTrue(adminStr.contains("Created At"));
        assertTrue(adminStr.contains("Updated At"));
    }
}
