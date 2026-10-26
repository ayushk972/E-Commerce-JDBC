package com.ecom.service;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.exception.*;
import com.ecom.model.CartItem;
import com.ecom.model.Customers;
import com.ecom.model.Products;
import com.ecom.repository.CartRepository;
import com.ecom.repository.CustomersRepository;
import com.ecom.repository.ProductsRepository;
import com.ecom.util.DataBaseConnection;

public class CartServiceTest {

    private CartRepository cartRepository;
    private CustomersRepository customersRepository;
    private ProductsRepository productsRepository;
    private ProductsService productsService;
    private CartService cartService;
    private int customerId;
    private int productId;

    @BeforeEach
    void setUp() {
        cartRepository = new CartRepository();
        customersRepository = new CustomersRepository();
        productsRepository = new ProductsRepository();
        productsService = new ProductsService(productsRepository);
        cartService = new CartService(cartRepository, productsService);

        String email = "cart_srv_" + System.currentTimeMillis() + "@test.com";
        customerId = customersRepository.addCustomer(new Customers(0, "CartUser", email, "Pass", "1234567890", "Street"));
        String pName = "CartTestProd_" + System.currentTimeMillis();
        productsRepository.addProduct(new Products(0, pName, "Tech", 50.0, 10, "CartBrand"));
        productId = productsRepository.findByName(pName).get(0).getProductId();
    }

    @AfterEach
    void tearDown() {
        if (customerId > 0) {
            cartRepository.clearCart(customerId);
            try (Connection con = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                pstmt.setInt(1, customerId);
                pstmt.executeUpdate();
            } catch (Exception ignored) {}
        }
        if (productId > 0) {
            productsRepository.deleteProduct(productId);
        }
    }

    @Test
    void testAddToCartAndValidation() {
        assertThrows(InvalidQuantityException.class, () -> cartService.addToCart(customerId, productId, 0));
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(customerId, productId, 20));

        assertDoesNotThrow(() -> cartService.addToCart(customerId, productId, 2));
        assertDoesNotThrow(() -> cartService.addToCart(customerId, productId, 3));
    }

    @Test
    void testIncreaseAndDecreaseQuantity() {
        cartService.addToCart(customerId, productId, 2);

        assertThrows(CartItemNotFoundException.class, () -> cartService.increaseQuantity(customerId, 999999, 1));
        assertThrows(InvalidQuantityException.class, () -> cartService.increaseQuantity(customerId, productId, -1));
        assertThrows(InsufficientStockException.class, () -> cartService.increaseQuantity(customerId, productId, 50));
        assertDoesNotThrow(() -> cartService.increaseQuantity(customerId, productId, 2));

        assertThrows(CartItemNotFoundException.class, () -> cartService.decreaseQuantity(customerId, 999999, 1));
        assertThrows(InvalidQuantityException.class, () -> cartService.decreaseQuantity(customerId, productId, -1));
        assertDoesNotThrow(() -> cartService.decreaseQuantity(customerId, productId, 1));
        assertDoesNotThrow(() -> cartService.decreaseQuantity(customerId, productId, 10));

        assertThrows(CartItemNotFoundException.class, () -> cartService.removeFromCart(customerId, 999999));
    }

    @Test
    void testViewAndClearCart() {
        assertDoesNotThrow(() -> cartService.viewCart(customerId));
        assertDoesNotThrow(() -> cartService.viewCart(new ArrayList<>()));

        cartService.addToCart(customerId, productId, 2);
        assertDoesNotThrow(() -> cartService.viewCart(customerId));
        assertDoesNotThrow(() -> cartService.viewCart(List.of(new CartItem(productId, "P", 50.0, 2))));

        assertEquals(100.0, cartService.calculateTotal(customerId), 0.01);
        assertEquals(1, cartService.getCartItems(customerId).size());
        assertDoesNotThrow(() -> cartService.clearCart(customerId));
        assertTrue(cartService.getCartItems(customerId).isEmpty());
    }
}
