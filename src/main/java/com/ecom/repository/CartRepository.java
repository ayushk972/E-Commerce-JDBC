package com.ecom.repository;

import java.sql.*;
import java.util.*;

import com.ecom.model.CartItem;
import com.ecom.util.DataBaseConnection;

public class CartRepository {

    // Add item to cart (or update quantity if already exists)
    public void addToCart(int customerId, CartItem item) {
        String query = "INSERT INTO cart (customer_id, product_id, quantity) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, item.getProductId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setInt(4, item.getQuantity());
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error adding to cart: " + e.getMessage());
        }
    }

    // Get all cart items for a customer (joined with products to get name/price)
    public List<CartItem> getCartItems(int customerId) {
        List<CartItem> items = new ArrayList<>();
        String query = "SELECT c.product_id, p.product_name, p.price, c.quantity " +
                       "FROM cart c JOIN products p ON c.product_id = p.product_id " +
                       "WHERE c.customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                items.add(new CartItem(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity")
                ));
            }

        } catch (Exception e) {
            System.out.println("Error fetching cart: " + e.getMessage());
        }
        return items;
    }

    // Remove item from cart
    public void removeFromCart(int customerId, int productId) {
        String query = "DELETE FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error removing from cart: " + e.getMessage());
        }
    }

    // Update quantity of a cart item
    public void updateQuantity(int customerId, int productId, int newQuantity) {
        String query = "UPDATE cart SET quantity = ? WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, customerId);
            pstmt.setInt(3, productId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error updating cart quantity: " + e.getMessage());
        }
    }

    // Clear cart
    public void clearCart(int customerId) {
        String query = "DELETE FROM cart WHERE customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error clearing cart: " + e.getMessage());
        }
    }

    // Check if product is in cart
    public boolean isInCart(int customerId, int productId) {
        String query = "SELECT COUNT(*) FROM cart WHERE customer_id = ? AND product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setInt(1, customerId);
            pstmt.setInt(2, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (Exception e) {
            System.out.println("Error checking cart: " + e.getMessage());
        }
        return false;
    }
}
