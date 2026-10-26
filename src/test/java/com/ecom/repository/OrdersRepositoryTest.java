package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.exception.InvalidOrderException;
import com.ecom.model.CartItem;
import com.ecom.model.Customers;
import com.ecom.model.Order;
import com.ecom.model.OrderStatus;
import com.ecom.util.DataBaseConnection;

public class OrdersRepositoryTest {

    private final OrdersRepository ordersRepo = new OrdersRepository();
    private final CustomersRepository customersRepo = new CustomersRepository();
    private int custId;
    private String testOrderId;

    @BeforeEach
    void setUp() {
        String testEmail = "ord_repo_" + System.currentTimeMillis() + "@test.com";
        custId = customersRepo.addCustomer(new Customers(0, "OrderCust", testEmail, "Pass", "1234567890", "Street"));
        testOrderId = "ORD-" + (System.currentTimeMillis() % 1000000000L);
    }

    @AfterEach
    void tearDown() {
        try (Connection con = DataBaseConnection.getConnection()) {
            if (testOrderId != null) {
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                    pstmt.setString(1, testOrderId); pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                    pstmt.setString(1, testOrderId); pstmt.executeUpdate();
                }
            }
            if (custId > 0) {
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                    pstmt.setInt(1, custId); pstmt.executeUpdate();
                }
            }
        } catch (Exception ignored) {}
    }

    @Test
    void testOrderCrud() {
        CartItem item = new CartItem(1, "iPhone 16", 999.0, 1);
        Order order = new Order(testOrderId, custId, List.of(item), 999.0);

        ordersRepo.saveOrder(order);

        Order found = ordersRepo.findById(testOrderId);
        assertNotNull(found);
        assertEquals(testOrderId, found.getOrderId());
        assertEquals(custId, found.getCustomerId());
        assertEquals(OrderStatus.PLACED, found.getOrderStatus());
        assertFalse(found.getItems().isEmpty());

        ordersRepo.updateOrderStatus(testOrderId, OrderStatus.CONFIRMED);
        Order updated = ordersRepo.findById(testOrderId);
        assertEquals(OrderStatus.CONFIRMED, updated.getOrderStatus());

        List<Order> byCust = ordersRepo.findByCustomerId(custId);
        assertFalse(byCust.isEmpty());

        List<Order> all = ordersRepo.getAllOrders();
        assertFalse(all.isEmpty());
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(InvalidOrderException.class, () -> ordersRepo.findById("NON_EXISTENT_ID_999"));
    }
}
