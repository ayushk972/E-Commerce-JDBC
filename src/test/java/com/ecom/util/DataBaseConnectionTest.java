package com.ecom.util;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

public class DataBaseConnectionTest {

    @Test
    void testGetConnection() throws Exception {
        try (Connection con = DataBaseConnection.getConnection();
             Statement stmt = con.createStatement()) {
            assertNotNull(con);
            assertFalse(con.isClosed());

            // Cleanup any stray test products from previous test runs
            stmt.executeUpdate("DELETE FROM cart WHERE customer_id >= 777777");
            stmt.executeUpdate("DELETE FROM products WHERE product_name LIKE 'ServiceTestProd%' OR product_name LIKE 'TestP_%' OR product_name LIKE 'TestKeyGen%' OR product_name LIKE 'JUnit%' OR product_name LIKE 'CartTestProd%' OR product_name LIKE 'OrdProd_%' OR product_name LIKE 'SrvProd_%' OR product_name LIKE 'CartRepoProd_%'");
            stmt.executeUpdate("DELETE FROM customers WHERE email LIKE 'junit_%' OR email LIKE 'ord_%' OR email LIKE 'cart_%' OR email LIKE 'cust_service_%'");
        }
    }

    @Test
    void testConstructor() {
        assertNotNull(new DataBaseConnection());
    }
}
