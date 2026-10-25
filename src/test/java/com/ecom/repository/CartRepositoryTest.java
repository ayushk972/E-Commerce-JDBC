package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.ecom.model.CartItem;
import com.ecom.util.DataBaseConnection;

public class CartRepositoryTest {

    private CartRepository cartRepository;
    private MockedStatic<DataBaseConnection> mockedDb;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        cartRepository = new CartRepository();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDb = mockStatic(DataBaseConnection.class);
        mockedDb.when(DataBaseConnection::getConnection).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
    }

    @AfterEach
    void tearDown() {
        mockedDb.close();
    }

    @Test
    void testAddToCart() throws Exception {
        CartItem item = new CartItem(1, "Mouse", 25.0, 2);
        when(mockStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> cartRepository.addToCart(101, item));
        verify(mockStatement, times(1)).executeUpdate();
    }

    @Test
    void testGetCartItems() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("product_id")).thenReturn(1);
        when(mockResultSet.getString("product_name")).thenReturn("Mouse");
        when(mockResultSet.getDouble("price")).thenReturn(25.0);
        when(mockResultSet.getInt("quantity")).thenReturn(2);

        List<CartItem> items = cartRepository.getCartItems(101);
        assertEquals(1, items.size());
        assertEquals("Mouse", items.get(0).getProductName());
    }

    @Test
    void testRemoveFromCartAndClear() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> cartRepository.removeFromCart(101, 1));
        assertDoesNotThrow(() -> cartRepository.clearCart(101));
        assertDoesNotThrow(() -> cartRepository.updateQuantity(101, 1, 5));
    }

    @Test
    void testIsInCart() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        assertTrue(cartRepository.isInCart(101, 1));
    }
}
