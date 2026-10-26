package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.exception.*;
import com.ecom.model.*;
import com.ecom.repository.*;
import com.ecom.util.DataBaseConnection;

public class OrdersServiceTest {

    private OrdersRepository ordersRepo;
    private CustomersRepository customersRepo;
    private ProductsRepository productsRepo;
    private CartRepository cartRepo;
    private CartService cartService;
    private ProductsService productsService;
    private OrdersService ordersService;

    private int customerId;
    private int productId;
    private String orderId;

    @BeforeEach
    void setUp() {
        ordersRepo = new OrdersRepository();
        customersRepo = new CustomersRepository();
        productsRepo = new ProductsRepository();
        cartRepo = new CartRepository();

        productsService = new ProductsService(productsRepo);
        cartService = new CartService(cartRepo, productsService);
        ordersService = new OrdersService(ordersRepo, cartService, productsService, productsRepo);

        String email = "ord_srv_" + System.currentTimeMillis() + "@test.com";
        customerId = customersRepo.addCustomer(new Customers(0, "OrdUser", email, "Pass", "1234567890", "Street"));
        String pName = "OrdProd_" + System.currentTimeMillis();
        productsRepo.addProduct(new Products(0, pName, "Tech", 100.0, 10, "OrdBrand"));
        productId = productsRepo.findByName(pName).get(0).getProductId();
    }

    @AfterEach
    void tearDown() {
        try (Connection con = DataBaseConnection.getConnection()) {
            if (orderId != null) {
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
                    pstmt.setString(1, orderId); pstmt.executeUpdate();
                }
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {
                    pstmt.setString(1, orderId); pstmt.executeUpdate();
                }
            }
            if (customerId > 0) {
                cartRepo.clearCart(customerId);
                try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                    pstmt.setInt(1, customerId); pstmt.executeUpdate();
                }
            }
            if (productId > 0) {
                productsRepo.deleteProduct(productId);
            }
        } catch (Exception ignored) {}
    }

    @Test
    void testPlaceOrderEmptyCart() {
        assertThrows(EmptyCartException.class, () -> ordersService.placeOrder(customerId));
    }

    @Test
    void testPlaceOrderAndCancelFlow() {
        cartService.addToCart(customerId, productId, 2);
        assertDoesNotThrow(() -> ordersService.placeOrder(customerId));

        List<Order> orders = ordersRepo.findByCustomerId(customerId);
        assertFalse(orders.isEmpty());
        orderId = orders.get(0).getOrderId();

        assertDoesNotThrow(() -> ordersService.viewCustomerOrders(customerId));
        assertDoesNotThrow(() -> ordersService.viewOrder(orderId));
        assertDoesNotThrow(() -> ordersService.viewAllOrdersAdmin());
        assertDoesNotThrow(() -> ordersService.viewOrderAdmin(orderId));

        assertThrows(UnauthorizedOrderAccessException.class, () -> ordersService.cancelOrder(orderId, 999999));
        assertDoesNotThrow(() -> ordersService.cancelOrder(orderId, customerId));
        assertThrows(OrderAlreadyCancelledException.class, () -> ordersService.cancelOrder(orderId, customerId));
    }

    @Test
    void testUpdateOrderStatusTransitions() {
        cartService.addToCart(customerId, productId, 1);
        ordersService.placeOrder(customerId);
        orderId = ordersRepo.findByCustomerId(customerId).get(0).getOrderId();

        assertDoesNotThrow(() -> ordersService.updateOrderStatus(orderId, OrderStatus.CONFIRMED));

        assertThrows(InvalidOrderStatusTransitionException.class,
            () -> ordersService.updateOrderStatus(orderId, OrderStatus.CONFIRMED));
        assertThrows(InvalidOrderStatusTransitionException.class,
            () -> ordersService.updateOrderStatus(orderId, OrderStatus.PLACED));

        assertDoesNotThrow(() -> ordersService.updateOrderStatus(orderId, OrderStatus.SHIPPED));
        assertDoesNotThrow(() -> ordersService.updateOrderStatus(orderId, OrderStatus.DELIVERED));

        assertThrows(OrderAlreadyDeliveredException.class, () -> ordersService.cancelOrder(orderId, customerId));
    }
}
