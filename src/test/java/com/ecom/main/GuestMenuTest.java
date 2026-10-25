package com.ecom.main;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.model.Customers;
import com.ecom.model.Products;
import com.ecom.service.CartService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

public class GuestMenuTest {

    private ProductsService productsService;
    private CartService cartService;
    private CustomerMenu customerMenu;
    private OrdersService ordersService;
    private GuestMenu guestMenu;

    @BeforeEach
    void setUp() {
        productsService = mock(ProductsService.class);
        cartService = mock(CartService.class);
        customerMenu = mock(CustomerMenu.class);
        ordersService = mock(OrdersService.class);
        guestMenu = new GuestMenu(productsService, cartService, customerMenu, ordersService);
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.setScanner(new Scanner(System.in));
    }

    private void runWithInput(String input) {
        ConsoleHelper.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
        guestMenu.show();
    }

    @Test
    void testBrowsingAndSearching() {
        String input = String.join("\n",
            "1",
            "2", "10",
            "3", "Headphones",
            "4", "Audio",
            "5", "Sony",
            "6", "50", "200",
            "0"
        ) + "\n";

        runWithInput(input);
        verify(productsService).viewAllProducts();
        verify(productsService).searchById(10);
        verify(productsService).searchByName("Headphones");
        verify(productsService).searchByCategory("Audio");
        verify(productsService).searchByBrand("Sony");
        verify(productsService).searchByPriceRange(50.0, 200.0);
    }

    @Test
    void testAddToCartAndQuantityHandling() {
        Products p = new Products(1, "Mouse", "Accessories", 25.0, 10, "Logitech");
        when(productsService.getProduct(1)).thenReturn(p);

        String input = String.join("\n",
            "7", "1", "-1", // triggers InvalidQuantityException catch
            "7", "1", "15", // triggers InsufficientStockException catch
            "7", "1", "2",  // adds 2
            "7", "1", "3",  // updates existing to 5
            "8",            // view cart
            "0"
        ) + "\n";

        runWithInput(input);
        verify(cartService, atLeastOnce()).viewCart(anyList());
    }

    @Test
    void testRemoveFromGuestCart() {
        Products p = new Products(1, "Mouse", "Accessories", 25.0, 10, "Logitech");
        when(productsService.getProduct(1)).thenReturn(p);

        String input = String.join("\n",
            "9",            // empty cart remove
            "7", "1", "2",  // add
            "9", "99",      // item not found in cart
            "9", "1",       // item found and removed
            "0"
        ) + "\n";

        runWithInput(input);
    }

    @Test
    void testPlaceOrderFlows() {
        Products p = new Products(1, "Mouse", "Accessories", 25.0, 10, "Logitech");
        when(productsService.getProduct(1)).thenReturn(p);
        Customers customer = new Customers(1, "Bob", "bob@example.com", "pass", "9999999999", "Addr");

        // Case 1: Place order with empty cart
        // Case 2: Add item, place order, auth returns null
        // Case 3: Add item, place order, auth succeeds, confirm 'no'
        // Case 4: Add item, place order, auth succeeds, confirm 'yes'
        when(customerMenu.authenticate()).thenReturn(null, customer, customer);

        String input = String.join("\n",
            "10",                  // empty cart
            "7", "1", "1", "10",   // add then place order, auth cancelled
            "7", "1", "1", "10", "no", // add then place order, confirm no
            "7", "1", "1", "10", "yes", // add then place order, confirm yes
            "99", // invalid choice
            "0"
        ) + "\n";

        runWithInput(input);
        verify(ordersService).placeOrder(customer.getCustomerId());
        verify(customerMenu, times(2)).showDashboard(customer);
    }
}
