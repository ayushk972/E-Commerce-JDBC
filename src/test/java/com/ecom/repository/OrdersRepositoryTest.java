package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.ecom.exception.InvalidOrderException;
import com.ecom.model.CartItem;
import com.ecom.model.Order;
import com.ecom.model.OrderStatus;
import com.ecom.util.DataBaseConnection;

public class OrdersRepositoryTest {

    private OrdersRepository repository;
    private MockedStatic<DataBaseConnection> mockedDb;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        repository = new OrdersRepository();
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

    private void setupOrderResultSet() throws Exception {
        when(mockResultSet.getString("order_id")).thenReturn("ORD-1001");
        when(mockResultSet.getInt("customer_id")).thenReturn(1);
        when(mockResultSet.getDouble("total_amount")).thenReturn(150.0);
        when(mockResultSet.getString("order_status")).thenReturn("PLACED");
        when(mockResultSet.getTimestamp("order_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Test
    void testSaveOrderSuccess() throws Exception {
        List<CartItem> items = List.of(new CartItem(1, "Mouse", 25.0, 2));
        Order order = new Order("ORD-1001", 1, items, 50.0);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.executeBatch()).thenReturn(new int[]{1});

        assertDoesNotThrow(() -> repository.saveOrder(order));
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
    }

    @Test
    void testSaveOrderException() throws Exception {
        List<CartItem> items = List.of(new CartItem(1, "Mouse", 25.0, 2));
        Order order = new Order("ORD-1001", 1, items, 50.0);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Order insert failed"));

        assertDoesNotThrow(() -> repository.saveOrder(order));
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false, true, false);
        setupOrderResultSet();
        when(mockResultSet.getInt("product_id")).thenReturn(1);
        when(mockResultSet.getString("product_name")).thenReturn("Mouse");
        when(mockResultSet.getDouble("price")).thenReturn(25.0);
        when(mockResultSet.getInt("quantity")).thenReturn(2);

        Order order = repository.findById("ORD-1001");
        assertNotNull(order);
        assertEquals("ORD-1001", order.getOrderId());
        assertEquals(OrderStatus.PLACED, order.getOrderStatus());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertThrows(InvalidOrderException.class, () -> repository.findById("NON_EXISTENT"));
    }

    @Test
    void testFindByCustomerId() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false, true, false);
        setupOrderResultSet();
        when(mockResultSet.getInt("product_id")).thenReturn(1);
        when(mockResultSet.getString("product_name")).thenReturn("Mouse");
        when(mockResultSet.getDouble("price")).thenReturn(25.0);
        when(mockResultSet.getInt("quantity")).thenReturn(2);

        List<Order> orders = repository.findByCustomerId(1);
        assertEquals(1, orders.size());
        assertEquals("ORD-1001", orders.get(0).getOrderId());
    }

    @Test
    void testGetAllOrders() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false, false);
        setupOrderResultSet();

        List<Order> orders = repository.getAllOrders();
        assertEquals(1, orders.size());
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repository.updateOrderStatus("ORD-1001", OrderStatus.SHIPPED));
        verify(mockStatement).executeUpdate();
    }
}
