package com.ecom.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class OrderStatusTest {

    @Test
    void testOrderStatusValues() {
        assertEquals(5, OrderStatus.values().length);
        assertEquals(OrderStatus.PLACED, OrderStatus.valueOf("PLACED"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.valueOf("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatus.valueOf("DELIVERED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }
}
