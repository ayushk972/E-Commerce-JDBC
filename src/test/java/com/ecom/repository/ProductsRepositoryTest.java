package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.ecom.exception.ProductNotFoundException;
import com.ecom.model.Products;
import com.ecom.util.DataBaseConnection;

public class ProductsRepositoryTest {

    private ProductsRepository repository;
    private MockedStatic<DataBaseConnection> mockedDb;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        repository = new ProductsRepository();
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedDb = mockStatic(DataBaseConnection.class);
        mockedDb.when(DataBaseConnection::getConnection).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
    }

    @AfterEach
    void tearDown() {
        mockedDb.close();
    }

    private void setupMockProductRow() throws Exception {
        when(mockResultSet.getInt("product_id")).thenReturn(1);
        when(mockResultSet.getString("product_name")).thenReturn("Laptop");
        when(mockResultSet.getString("category")).thenReturn("Electronics");
        when(mockResultSet.getDouble("price")).thenReturn(999.99);
        when(mockResultSet.getInt("quantity")).thenReturn(10);
        when(mockResultSet.getString("brand")).thenReturn("Dell");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockProductRow();

        List<Products> list = repository.getAllProducts();
        assertEquals(1, list.size());
        assertEquals("Laptop", list.get(0).getProductName());
    }

    @Test
    void testGetAllProductsException() throws Exception {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("DB error"));
        List<Products> list = repository.getAllProducts();
        assertTrue(list.isEmpty());
    }

    @Test
    void testAddProductSuccess() throws Exception {
        ResultSet mockKeys = mock(ResultSet.class);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getInt(1)).thenReturn(42);

        Products p = new Products(0, "Phone", "Mobile", 500.0, 5, "Samsung");
        int id = repository.addProduct(p);
        assertEquals(42, id);
    }

    @Test
    void testAddProductFailure() throws Exception {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Fail"));
        Products p = new Products(0, "Phone", "Mobile", 500.0, 5, "Samsung");
        int id = repository.addProduct(p);
        assertEquals(-1, id);
    }

    @Test
    void testAddProductsBatchSuccess() throws Exception {
        when(mockStatement.executeBatch()).thenReturn(new int[]{1, 1});
        List<Products> list = List.of(
            new Products(0, "P1", "C1", 10.0, 1, "B1"),
            new Products(0, "P2", "C2", 20.0, 2, "B2")
        );
        int count = repository.addProductsBatch(list);
        assertEquals(2, count);
        verify(mockConnection).commit();
    }

    @Test
    void testAddProductsBatchFailure() throws Exception {
        when(mockStatement.executeBatch()).thenThrow(new SQLException("Batch err"));
        List<Products> list = List.of(new Products(0, "P1", "C1", 10.0, 1, "B1"));
        int count = repository.addProductsBatch(list);
        assertEquals(0, count);
        verify(mockConnection).rollback();
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockProductRow();

        Products p = repository.findById(1);
        assertNotNull(p);
        assertEquals("Laptop", p.getProductName());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertThrows(ProductNotFoundException.class, () -> repository.findById(99));
    }

    @Test
    void testFindByNameAndCategoryAndBrand() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockProductRow();

        assertEquals(1, repository.findByName("laptop").size());
        when(mockResultSet.next()).thenReturn(true, false);
        assertEquals(1, repository.findByCategory("elec").size());
        when(mockResultSet.next()).thenReturn(true, false);
        assertEquals(1, repository.findByBrand("dell").size());
    }

    @Test
    void testFindByPriceRange() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockProductRow();

        List<Products> list = repository.findByPriceRange(100.0, 1500.0);
        assertEquals(1, list.size());
    }

    @Test
    void testUpdatePriceAndQuantity() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repository.updatePrice(1, 899.99));
        assertDoesNotThrow(() -> repository.updateQuantity(1, 20));
        verify(mockStatement, times(2)).executeUpdate();
    }

    @Test
    void testDeleteProduct() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockProductRow();
        when(mockStatement.executeUpdate()).thenReturn(1);

        assertDoesNotThrow(() -> repository.deleteProduct(1));
    }

    @Test
    void testExistsById() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        assertTrue(repository.existsById(1));

        when(mockResultSet.getInt(1)).thenReturn(0);
        assertFalse(repository.existsById(2));
    }
}
