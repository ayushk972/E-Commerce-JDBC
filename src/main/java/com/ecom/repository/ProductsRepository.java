package com.ecom.repository;

import java.sql.*;
import java.util.*;

import com.ecom.exception.ProductNotFoundException;
import com.ecom.model.Products;
import com.ecom.util.DataBaseConnection;

public class ProductsRepository {

    // Get all products
    public List<Products> getAllProducts() {
        List<Products> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error fetching products: " + e.getMessage());
        }
        return products;
    }

    // Add single product
    public int addProduct(Products product) {
        String query = "INSERT INTO products (product_name, category, price, quantity, brand) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getCategory());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setString(5, product.getBrand());
            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (Exception e) {
            System.out.println("Error adding product: " + e.getMessage());
        }
        return -1;
    }

    // Batch add products using JDBC batch processing with transaction rollback
    public int addProductsBatch(List<Products> productsList) {
        String query = "INSERT INTO products (product_name, category, price, quantity, brand) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DataBaseConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement pstmt = con.prepareStatement(query)) {
                for (Products p : productsList) {
                    pstmt.setString(1, p.getProductName());
                    pstmt.setString(2, p.getCategory());
                    pstmt.setDouble(3, p.getPrice());
                    pstmt.setInt(4, p.getQuantity());
                    pstmt.setString(5, p.getBrand());
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                con.commit();
                return results.length;
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println("Error in batch product insertion: " + e.getMessage());
            return 0;
        }
    }

    // Find by ID
    public Products findById(int id) {
        String query = "SELECT * FROM products WHERE product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error finding product: " + e.getMessage());
        }
        throw new ProductNotFoundException("ProductNotFoundException: Product with ID " + id + " does not exist.");
    }

    private List<Products> findByColumnLike(String column, String value) {
        List<Products> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE LOWER(" + column + ") LIKE ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, "%" + value.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) products.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error searching products: " + e.getMessage());
        }
        return products;
    }

    public List<Products> findByName(String name) {
        return findByColumnLike("product_name", name);
    }

    public List<Products> findByCategory(String category) {
        return findByColumnLike("category", category);
    }

    public List<Products> findByBrand(String brand) {
        return findByColumnLike("brand", brand);
    }

    // Find by price range
    public List<Products> findByPriceRange(double min, double max) {
        List<Products> products = new ArrayList<>();
        String query = "SELECT * FROM products WHERE price >= ? AND price <= ? ORDER BY price";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setDouble(1, min);
            pstmt.setDouble(2, max);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) products.add(mapRow(rs));
        } catch (Exception e) {
            System.out.println("Error searching by price range: " + e.getMessage());
        }
        return products;
    }

    // Update price
    public void updatePrice(int id, double newPrice) {
        String query = "UPDATE products SET price = ? WHERE product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating price: " + e.getMessage());
        }
    }

    // Update quantity
    public void updateQuantity(int id, int newQuantity) {
        String query = "UPDATE products SET quantity = ? WHERE product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating quantity: " + e.getMessage());
        }
    }

    // Delete product
    public void deleteProduct(int id) {
        findById(id);
        String query = "DELETE FROM products WHERE product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
        }
    }

    // Check exists
    public boolean existsById(int id) {
        String query = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) {
            System.out.println("Error checking product existence: " + e.getMessage());
        }
        return false;
    }

    // Helper to map ResultSet row to Products object
    private Products mapRow(ResultSet rs) throws SQLException {
        Timestamp cTs = null, uTs = null;
        try { cTs = rs.getTimestamp("created_at"); } catch (Exception ignored) {}
        try { uTs = rs.getTimestamp("updated_at"); } catch (Exception ignored) {}
        return new Products(
            rs.getInt("product_id"), rs.getString("product_name"), rs.getString("category"),
            rs.getDouble("price"), rs.getInt("quantity"), rs.getString("brand"),
            cTs != null ? cTs.toLocalDateTime() : null,
            uTs != null ? uTs.toLocalDateTime() : null
        );
    }
}
