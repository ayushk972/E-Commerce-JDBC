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
import com.ecom.model.CartItem;
import com.ecom.model.Products;
import com.ecom.repository.CartRepository;

public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductsService productsService;

    @InjectMocks
    private CartService cartService;

    private Products product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new Products(1, "Mouse", "Accessories", 25.0, 10, "Logi");
        cartItem = new CartItem(1, "Mouse", 25.0, 2);
    }

    @Test
    void testAddToCartSuccess() {
        when(productsService.getProduct(1)).thenReturn(product);
        when(cartRepository.getCartItems(101)).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> cartService.addToCart(101, 1, 2));
        verify(cartRepository, times(1)).addToCart(eq(101), any(CartItem.class));
    }

    @Test
    void testAddToCartExistingItemAndStockCheck() {
        when(productsService.getProduct(1)).thenReturn(product);
        when(cartRepository.getCartItems(101)).thenReturn(List.of(cartItem));

        assertDoesNotThrow(() -> cartService.addToCart(101, 1, 3));
        assertThrows(InvalidQuantityException.class, () -> cartService.addToCart(101, 1, 0));
        assertThrows(InsufficientStockException.class, () -> cartService.addToCart(101, 1, 9));
    }

    @Test
    void testRemoveFromCart() {
        when(cartRepository.isInCart(101, 1)).thenReturn(true);
        when(cartRepository.isInCart(101, 99)).thenReturn(false);

        assertDoesNotThrow(() -> cartService.removeFromCart(101, 1));
        assertThrows(CartItemNotFoundException.class, () -> cartService.removeFromCart(101, 99));
    }

    @Test
    void testQuantityAdjustments() {
        when(cartRepository.getCartItems(101)).thenReturn(List.of(cartItem));
        when(productsService.getProduct(1)).thenReturn(product);

        assertDoesNotThrow(() -> cartService.increaseQuantity(101, 1, 2));
        assertThrows(InvalidQuantityException.class, () -> cartService.increaseQuantity(101, 1, -1));
        assertThrows(InsufficientStockException.class, () -> cartService.increaseQuantity(101, 1, 20));

        assertDoesNotThrow(() -> cartService.decreaseQuantity(101, 1, 1));
        assertDoesNotThrow(() -> cartService.decreaseQuantity(101, 1, 5)); // to 0 or below, removes
        assertThrows(InvalidQuantityException.class, () -> cartService.decreaseQuantity(101, 1, 0));
    }

    @Test
    void testViewAndCalculateTotal() {
        when(cartRepository.getCartItems(101)).thenReturn(new ArrayList<>());
        cartService.viewCart(101);
        cartService.viewCart((List<CartItem>) null);

        when(cartRepository.getCartItems(101)).thenReturn(List.of(cartItem));
        cartService.viewCart(101);
        cartService.viewCart(List.of(cartItem));

        assertEquals(50.0, cartService.calculateTotal(101));
        assertDoesNotThrow(() -> cartService.clearCart(101));
        assertEquals(1, cartService.getCartItems(101).size());
    }
}
