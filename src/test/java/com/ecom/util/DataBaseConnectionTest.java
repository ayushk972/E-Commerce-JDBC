package com.ecom.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ecom.exception.DatabaseConnectionException;

public class DataBaseConnectionTest {

    @Test
    void testGetConnectionFailureThrowsDatabaseConnectionException() {
        SQLException simulatedException = new SQLException("Simulated connection failure");
        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                  .thenThrow(simulatedException);

            assertThrows(DatabaseConnectionException.class, DataBaseConnection::getConnection);
        }
    }

    @Test
    void testGetConnectionSuccess() {
        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            Connection mockCon = Mockito.mock(Connection.class);
            mocked.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                  .thenReturn(mockCon);

            Connection con = DataBaseConnection.getConnection();
            assertNotNull(con);
        }
    }
}
