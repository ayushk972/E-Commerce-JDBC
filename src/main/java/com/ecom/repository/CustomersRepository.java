package com.ecom.repository;

import java.sql.*;
import java.util.*;

import com.ecom.exception.CustomerNotFoundException;
import com.ecom.model.Customers;
import com.ecom.util.DataBaseConnection;

public class CustomersRepository {

    // Add customer (with password)
    public int addCustomer(Customers customer) {
        String query = "INSERT INTO customers (name, email, password, mobile, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPassword());
            pstmt.setString(4, customer.getMobile());
            pstmt.setString(5, customer.getAddress());
            pstmt.executeUpdate();

            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLIntegrityConstraintViolationException e) {
            throw new com.ecom.exception.DuplicateEmailException(
                "DuplicateEmailException: Email '" + customer.getEmail() + "' is already registered.");
        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
        return -1;
    }

    // Find by ID
    public Customers findById(int id) {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        throw new CustomerNotFoundException(
            "CustomerNotFoundException: Customer with ID " + id + " does not exist.");
    }

    // Find by email
    public Optional<Customers> findByEmail(String email) {
        String query = "SELECT * FROM customers WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));

        } catch (Exception e) {
            System.out.println("Error finding customer by email: " + e.getMessage());
        }
        return Optional.empty();
    }

    // Check email exists
    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    // Get all customers
    public List<Customers> getAllCustomers() {
        List<Customers> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY customer_id";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) customers.add(mapRow(rs));

        } catch (Exception e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }
        return customers;
    }

    // Update address
    public void updateAddress(int id, String newAddress) {
        String query = "UPDATE customers SET address = ? WHERE customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, newAddress);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error updating address: " + e.getMessage());
        }
    }

    // Update password
    public void updatePassword(int id, String newPassword) {
        String query = "UPDATE customers SET password = ? WHERE customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, newPassword);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
        }
    }

    // Helper
    private Customers mapRow(ResultSet rs) throws SQLException {
        Timestamp cTs = null, uTs = null;
        try { cTs = rs.getTimestamp("created_at"); } catch (Exception ignored) {}
        try { uTs = rs.getTimestamp("updated_at"); } catch (Exception ignored) {}
        return new Customers(
            rs.getInt("customer_id"), rs.getString("name"), rs.getString("email"),
            rs.getString("password"), rs.getString("mobile"), rs.getString("address"),
            cTs != null ? cTs.toLocalDateTime() : null,
            uTs != null ? uTs.toLocalDateTime() : null
        );
    }
}
