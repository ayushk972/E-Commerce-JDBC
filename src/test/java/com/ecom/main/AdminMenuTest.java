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
import com.ecom.service.CustomersService;
import com.ecom.service.OrdersService;
import com.ecom.service.ProductsService;

public class AdminMenuTest {

    private ProductsService productsService;
    private CustomersService customersService;
    private OrdersService ordersService;
    private AdminMenu adminMenu;

    @BeforeEach
    void setUp() {
        productsService = mock(ProductsService.class);
        customersService = mock(CustomersService.class);
        ordersService = mock(OrdersService.class);
        adminMenu = new AdminMenu(productsService, customersService, ordersService);
    }

    @AfterEach
    void tearDown() {
        ConsoleHelper.setScanner(new Scanner(System.in));
    }

    private void runWithInput(String input) {
        ConsoleHelper.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
        adminMenu.show();
    }

    @Test
    void testViewAndSearchProducts() {
        String input = String.join("\n",
            "1",
            "4", "5",
            "5", "Laptop",
            "6", "Electronics",
            "7", "Dell",
            "8", "100", "500",
            "0"
        ) + "\n";

        runWithInput(input);
        verify(productsService).viewAllProductsAdmin();
        verify(productsService).searchByIdAdmin(5);
        verify(productsService).searchByName("Laptop");
        verify(productsService).searchByCategory("Electronics");
        verify(productsService).searchByBrand("Dell");
        verify(productsService).searchByPriceRange(100.0, 500.0);
    }

    @Test
    void testAddAndModifyProducts() {
        String input = String.join("\n",
            "2", "Mouse", "Accessories", "Logitech", "25.0", "10",
            "3", "dummy/path.csv",
            "9", "1", "29.99",
            "10", "1", "15",
            "11", "1",
            "0"
        ) + "\n";

        runWithInput(input);
        verify(productsService).addProduct("Mouse", "Accessories", 25.0, 10, "Logitech");
        verify(productsService).addProductsFromFile("dummy/path.csv");
        verify(productsService).updatePrice(1, 29.99);
        verify(productsService).updateQuantity(1, 15);
        verify(productsService).deleteProduct(1);
    }

    @Test
    void testCustomerManagement() {
        Customers c = new Customers(1, "Alice", "alice@example.com", "pass", "9999999999", "Addr");
        when(customersService.searchById(1)).thenReturn(c);

        String input = String.join("\n",
            "12",
            "13", "1",
            "0"
        ) + "\n";

        runWithInput(input);
        verify(customersService).viewAllCustomersAdmin();
        verify(customersService).searchById(1);
    }

    @Test
    void testOrderManagementAndExceptions() {
        String input = String.join("\n",
            "14",
            "15", "ORD-1",
            "16", "ORD-1", "1",
            "16", "ORD-1", "2",
            "16", "ORD-1", "3",
            "16", "ORD-1", "99",
            "999",
            "0"
        ) + "\n";

        runWithInput(input);
        verify(ordersService).viewAllOrdersAdmin();
        verify(ordersService).viewOrderAdmin("ORD-1");
        verify(ordersService, atLeastOnce()).updateOrderStatus(eq("ORD-1"), any());
    }
}
