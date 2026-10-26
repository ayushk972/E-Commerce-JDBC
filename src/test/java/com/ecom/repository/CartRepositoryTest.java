package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecom.model.CartItem;
import com.ecom.model.Customers;
import com.ecom.model.Products;
import com.ecom.util.DataBaseConnection;

public class CartRepositoryTest {

    private final CartRepository cartRepo = new CartRepository();
    private final CustomersRepository customersRepo = new CustomersRepository();
    private final ProductsRepository productsRepo = new ProductsRepository();
    private int testCustomerId;
    private int prodId;

    @BeforeEach
    void setUp() {
        String email = "cart_repo_" + System.currentTimeMillis() + "@test.com";
        testCustomerId = customersRepo.addCustomer(new Customers(0, "CartCust", email, "Pass", "1234567890", "Street"));
        String pName = "CartRepoProd_" + System.currentTimeMillis();
        productsRepo.addProduct(new Products(0, pName, "Tech", 25.0, 10, "Brand"));
        prodId = productsRepo.findByName(pName).get(0).getProductId();
    }

    @AfterEach
    void tearDown() {
        cartRepo.clearCart(testCustomerId);
        if (prodId > 0) {
            productsRepo.deleteProduct(prodId);
        }
        if (testCustomerId > 0) {
            try (Connection con = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                pstmt.setInt(1, testCustomerId);
                pstmt.executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testCartOperations() {
        CartItem item = new CartItem(prodId, "CartRepoProd", 25.0, 2);

        cartRepo.addToCart(testCustomerId, item);
        assertTrue(cartRepo.isInCart(testCustomerId, prodId));

        List<CartItem> items = cartRepo.getCartItems(testCustomerId);
        assertFalse(items.isEmpty());
        assertEquals(prodId, items.get(0).getProductId());

        cartRepo.updateQuantity(testCustomerId, prodId, 5);
        List<CartItem> updatedItems = cartRepo.getCartItems(testCustomerId);
        assertEquals(5, updatedItems.get(0).getQuantity());

        cartRepo.removeFromCart(testCustomerId, prodId);
        assertFalse(cartRepo.isInCart(testCustomerId, prodId));

        cartRepo.addToCart(testCustomerId, item);
        cartRepo.clearCart(testCustomerId);
        assertTrue(cartRepo.getCartItems(testCustomerId).isEmpty());
    }
}
