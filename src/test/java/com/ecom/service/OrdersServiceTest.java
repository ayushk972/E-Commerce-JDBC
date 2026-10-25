package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.ecom.exception.*;
import com.ecom.model.*;
import com.ecom.repository.OrdersRepository;
import com.ecom.repository.ProductsRepository;

public class OrdersServiceTest {

    @Mock
    private OrdersRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductsService productsService;

    @Mock
    private ProductsRepository productsRepoRef;

    @InjectMocks
    private OrdersService ordersService;

    private Products product;
    private CartItem cartItem;
    private Order placedOrder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new Products(1, "Monitor", "Electronics", 200.0, 5, "Dell");
        cartItem = new CartItem(1, "Monitor", 200.0, 2);
        placedOrder = new Order("ORD-1", 101, List.of(cartItem), 400.0);
    }

    @Test
    void testPlaceOrderSuccess() {
        when(cartService.getCartItems(101)).thenReturn(List.of(cartItem));
        when(productsService.getProduct(1)).thenReturn(product);
        when(cartService.calculateTotal(101)).thenReturn(400.0);

        String orderId = ordersService.placeOrder(101);
        assertNotNull(orderId);
        verify(productsRepoRef, times(1)).updateQuantity(1, 3);
        verify(orderRepository, times(1)).saveOrder(any(Order.class));
        verify(cartService, times(1)).clearCart(101);
    }

    @Test
    void testPlaceOrderValidationFailures() {
        when(cartService.getCartItems(101)).thenReturn(new ArrayList<>());
        assertThrows(EmptyCartException.class, () -> ordersService.placeOrder(101));

        CartItem overStockItem = new CartItem(1, "Monitor", 200.0, 10);
        when(cartService.getCartItems(101)).thenReturn(List.of(overStockItem));
        when(productsService.getProduct(1)).thenReturn(product); // stock is 5
        assertThrows(InsufficientStockException.class, () -> ordersService.placeOrder(101));
    }

    @Test
    void testViewOrders() {
        when(orderRepository.findById("ORD-1")).thenReturn(placedOrder);
        when(orderRepository.findByCustomerId(101)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> ordersService.viewOrder("ORD-1"));
        assertDoesNotThrow(() -> ordersService.viewOrderAdmin("ORD-1"));
        ordersService.viewCustomerOrders(101);

        when(orderRepository.findByCustomerId(101)).thenReturn(List.of(placedOrder));
        ordersService.viewCustomerOrders(101);

        when(orderRepository.getAllOrders()).thenReturn(new ArrayList<>());
        ordersService.viewAllOrdersAdmin();
        when(orderRepository.getAllOrders()).thenReturn(List.of(placedOrder));
        ordersService.viewAllOrdersAdmin();
    }

    @Test
    void testCancelOrder() {
        when(orderRepository.findById("ORD-1")).thenReturn(placedOrder);
        when(productsService.getProduct(1)).thenReturn(product);

        // Unauthorized
        assertThrows(UnauthorizedOrderAccessException.class, () -> ordersService.cancelOrder("ORD-1", 999));

        // Already Delivered
        placedOrder.setOrderStatus(OrderStatus.DELIVERED);
        assertThrows(OrderAlreadyDeliveredException.class, () -> ordersService.cancelOrder("ORD-1", 101));

        // Already Cancelled
        placedOrder.setOrderStatus(OrderStatus.CANCELLED);
        assertThrows(OrderAlreadyCancelledException.class, () -> ordersService.cancelOrder("ORD-1", 101));

        // Success
        placedOrder.setOrderStatus(OrderStatus.PLACED);
        assertDoesNotThrow(() -> ordersService.cancelOrder("ORD-1", 101));
        verify(productsRepoRef, times(1)).updateQuantity(1, 7);
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById("ORD-1")).thenReturn(placedOrder);

        // Forward transition: PLACED -> CONFIRMED
        assertDoesNotThrow(() -> ordersService.updateOrderStatus("ORD-1", OrderStatus.CONFIRMED));

        // Backward transition: PLACED -> PLACED
        assertThrows(InvalidOrderStatusTransitionException.class,
            () -> ordersService.updateOrderStatus("ORD-1", OrderStatus.PLACED));

        // Cancelled order cannot be updated
        placedOrder.setOrderStatus(OrderStatus.CANCELLED);
        assertThrows(OrderAlreadyCancelledException.class,
            () -> ordersService.updateOrderStatus("ORD-1", OrderStatus.CONFIRMED));
    }
}
