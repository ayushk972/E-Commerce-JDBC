package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.ecom.exception.CustomerNotFoundException;
import com.ecom.exception.DuplicateEmailException;
import com.ecom.model.Customers;
import com.ecom.util.DataBaseConnection;

public class CustomersRepositoryTest {

    private CustomersRepository repository;
    private MockedStatic<DataBaseConnection> mockedDb;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        repository = new CustomersRepository();
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

    private void setupMockCustomerRow() throws Exception {
        when(mockResultSet.getInt("customer_id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("John Doe");
        when(mockResultSet.getString("email")).thenReturn("john@example.com");
        when(mockResultSet.getString("password")).thenReturn("Pass123");
        when(mockResultSet.getString("mobile")).thenReturn("9876543210");
        when(mockResultSet.getString("address")).thenReturn("123 Main St");
        when(mockResultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("updated_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
    }

    @Test
    void testAddCustomerSuccess() throws Exception {
        ResultSet mockKeys = mock(ResultSet.class);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getInt(1)).thenReturn(101);

        Customers c = new Customers(0, "John", "john@example.com", "Pass123", "9876543210", "Address");
        int id = repository.addCustomer(c);
        assertEquals(101, id);
    }

    @Test
    void testAddCustomerDuplicateEmail() throws Exception {
        when(mockStatement.executeUpdate()).thenThrow(new SQLIntegrityConstraintViolationException("Duplicate"));
        Customers c = new Customers(0, "John", "dup@example.com", "Pass123", "9876543210", "Address");
        assertThrows(DuplicateEmailException.class, () -> repository.addCustomer(c));
    }

    @Test
    void testAddCustomerGeneralException() throws Exception {
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Generic error"));
        Customers c = new Customers(0, "John", "err@example.com", "Pass123", "9876543210", "Address");
        int id = repository.addCustomer(c);
        assertEquals(-1, id);
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockCustomerRow();

        Customers c = repository.findById(1);
        assertNotNull(c);
        assertEquals("John Doe", c.getName());
    }

    @Test
    void testFindByIdNotFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        assertThrows(CustomerNotFoundException.class, () -> repository.findById(99));
    }

    @Test
    void testFindByEmailFoundAndNotFound() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockCustomerRow();

        Optional<Customers> opt = repository.findByEmail("john@example.com");
        assertTrue(opt.isPresent());
        assertEquals("John Doe", opt.get().getName());

        when(mockResultSet.next()).thenReturn(false);
        Optional<Customers> notFound = repository.findByEmail("other@example.com");
        assertTrue(notFound.isEmpty());
    }

    @Test
    void testEmailExists() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        setupMockCustomerRow();

        assertTrue(repository.emailExists("john@example.com"));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        setupMockCustomerRow();

        List<Customers> list = repository.getAllCustomers();
        assertEquals(1, list.size());
    }

    @Test
    void testGetAllCustomersException() throws Exception {
        when(mockStatement.executeQuery()).thenThrow(new SQLException("DB err"));
        List<Customers> list = repository.getAllCustomers();
        assertTrue(list.isEmpty());
    }

    @Test
    void testUpdateAddressAndPassword() throws Exception {
        when(mockStatement.executeUpdate()).thenReturn(1);
        assertDoesNotThrow(() -> repository.updateAddress(1, "New Address"));
        assertDoesNotThrow(() -> repository.updatePassword(1, "NewPassword123"));
        verify(mockStatement, times(2)).executeUpdate();
    }
}
