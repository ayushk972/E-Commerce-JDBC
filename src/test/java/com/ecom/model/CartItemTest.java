package com.ecom.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CartItemTest {

    @Test
    void testCartItemCreationAndCalculations() {
        CartItem item = new CartItem(10, "Keyboard", 75.50, 2);
        assertEquals(10, item.getProductId());
        assertEquals("Keyboard", item.getProductName());
        assertEquals(75.50, item.getPrice());
        assertEquals(2, item.getQuantity());
        assertEquals(151.0, item.getTotalPrice());

        item.setQuantity(4);
        assertEquals(4, item.getQuantity());
        assertEquals(302.0, item.getTotalPrice());

        String str = item.toString();
        assertTrue(str.contains("Keyboard"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("302.00"));
    }
}
