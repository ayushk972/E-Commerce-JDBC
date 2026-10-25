package com.ecom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProductsTest {

    private Products product;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        product = new Products(1, "Laptop", "Electronics", 999.99, 10, "Dell", now, now);
    }

    @Test
    void testDefaultConstructor() {
        Products p = new Products(2, "Phone", "Electronics", 499.99, 5, "Apple");
        assertEquals(2, p.getProductId());
        assertEquals("Phone", p.getProductName());
        assertEquals("Electronics", p.getCategory());
        assertEquals(499.99, p.getPrice());
        assertEquals(5, p.getQuantity());
        assertEquals("Apple", p.getBrand());
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        product.setPrice(1099.99);
        product.setQuantity(8);
        LocalDateTime updated = LocalDateTime.now().plusDays(1);
        product.setUpdatedAt(updated);
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        product.setCreatedAt(created);

        assertEquals(1, product.getProductId());
        assertEquals("Laptop", product.getProductName());
        assertEquals("Electronics", product.getCategory());
        assertEquals(1099.99, product.getPrice());
        assertEquals(8, product.getQuantity());
        assertEquals("Dell", product.getBrand());
        assertEquals(created, product.getCreatedAt());
        assertEquals(updated, product.getUpdatedAt());
    }

    @Test
    void testToStringCustomerView() {
        String output = product.toString();
        assertTrue(output.contains("Laptop"));
        assertTrue(output.contains("Dell"));
        assertTrue(output.contains("999.99"));
        assertFalse(output.contains("Created At"));
        assertFalse(output.contains("Updated At"));
    }

    @Test
    void testToAdminString() {
        String adminOutput = product.toAdminString();
        assertTrue(adminOutput.contains("Laptop"));
        assertTrue(adminOutput.contains("Created At"));
        assertTrue(adminOutput.contains("Updated At"));
    }
}
