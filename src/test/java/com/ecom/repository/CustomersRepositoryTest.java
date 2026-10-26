package com.ecom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ecom.exception.CustomerNotFoundException;
import com.ecom.exception.DuplicateEmailException;
import com.ecom.model.Customers;
import com.ecom.util.DataBaseConnection;

public class CustomersRepositoryTest {

    private final CustomersRepository repository = new CustomersRepository();

    @Test
    void testCustomerCrudAndSearch() {
        String testEmail = "junit_" + System.currentTimeMillis() + "@test.com";
        Customers c = new Customers(0, "JUnitUser", testEmail, "Pass123", "9876543210", "123 Street");
        int id = repository.addCustomer(c);
        assertTrue(id > 0, "Generated ID should be positive");

        try {
            Customers found = repository.findById(id);
            assertNotNull(found);
            assertEquals("JUnitUser", found.getName());
            assertEquals(testEmail, found.getEmail());

            assertTrue(repository.emailExists(testEmail));
            Optional<Customers> opt = repository.findByEmail(testEmail);
            assertTrue(opt.isPresent());

            repository.updateAddress(id, "456 Avenue");
            repository.updatePassword(id, "NewPass123");

            Customers updated = repository.findById(id);
            assertEquals("456 Avenue", updated.getAddress());
            assertEquals("NewPass123", updated.getPassword());

            List<Customers> all = repository.getAllCustomers();
            assertFalse(all.isEmpty());

            assertThrows(DuplicateEmailException.class, () -> repository.addCustomer(
                new Customers(0, "DupUser", testEmail, "P", "111", "Addr")
            ));
        } finally {
            try (Connection con = DataBaseConnection.getConnection();
                 PreparedStatement pstmt = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (Exception ignored) {}
        }
    }

    @Test
    void testFindByIdNotFound() {
        assertThrows(CustomerNotFoundException.class, () -> repository.findById(-999));
    }

    @Test
    void testFindByEmailNotFound() {
        assertFalse(repository.findByEmail("non_existent_email_123@xyz.com").isPresent());
    }
}
