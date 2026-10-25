package com.ecom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderTest {

    private Order order;
    private List<CartItem> items;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        items = new ArrayList<>();
        items.add(new CartItem(1, "Product 1", 50.0, 2));
        now = LocalDateTime.now();
        order = new Order("ORD-12345", 101, items, 100.0, now, now);
    }

    @Test
    void testStandardConstructor() {
        Order o = new Order("ORD-99999", 102, items, 100.0);
        assertEquals("ORD-99999", o.getOrderId());
        assertEquals(102, o.getCustomerId());
        assertEquals(OrderStatus.PLACED, o.getOrderStatus());
        assertEquals(1, o.getItems().size());
        assertEquals(100.0, o.getTotalqty());
        assertNotNull(o.getOrderDate());
        assertNotNull(o.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        order.setOrderStatus(OrderStatus.SHIPPED);
        LocalDateTime updated = LocalDateTime.now().plusHours(1);
        order.setUpdatedAt(updated);

        assertEquals("ORD-12345", order.getOrderId());
        assertEquals(101, order.getCustomerId());
        assertEquals(100.0, order.getTotalqty());
        assertEquals(OrderStatus.SHIPPED, order.getOrderStatus());
        assertEquals(now, order.getOrderDate());
        assertEquals(updated, order.getUpdatedAt());
    }

    @Test
    void testToStringCustomerView() {
        String str = order.toString();
        assertTrue(str.contains("ORD-12345"));
        assertTrue(str.contains("Ordered At"));
        assertTrue(str.contains("PLACED"));
        assertFalse(str.contains("Updated At"));
    }

    @Test
    void testToAdminString() {
        String adminStr = order.toAdminString();
        assertTrue(adminStr.contains("ORD-12345"));
        assertTrue(adminStr.contains("Ordered At"));
        assertTrue(adminStr.contains("Updated At"));
        assertTrue(adminStr.contains("Product 1"));
    }
}
