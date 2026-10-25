package com.ecom.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import com.ecom.exception.InvalidOrderException;
import com.ecom.model.CartItem;
import com.ecom.model.Order;
import com.ecom.model.OrderStatus;
import com.ecom.util.DataBaseConnection;

public class OrdersRepository {

    // Save order (and its items)
    public void saveOrder(Order order) {
        String orderQuery = "INSERT INTO orders (order_id, customer_id, total_amount, order_status, order_date) VALUES (?, ?, ?, ?, ?)";
        String itemQuery  = "INSERT INTO order_items (order_id, product_id, product_name, price, quantity, total_price) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DataBaseConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement orderStmt = con.prepareStatement(orderQuery)) {
                orderStmt.setString(1, order.getOrderId());
                orderStmt.setInt(2, order.getCustomerId());
                orderStmt.setDouble(3, order.getTotalqty());
                orderStmt.setString(4, order.getOrderStatus().name());
                orderStmt.setTimestamp(5, Timestamp.valueOf(order.getOrderDate()));
                orderStmt.executeUpdate();
            }

            try (PreparedStatement itemStmt = con.prepareStatement(itemQuery)) {
                for (CartItem item : order.getItems()) {
                    itemStmt.setString(1, order.getOrderId());
                    itemStmt.setInt(2, item.getProductId());
                    itemStmt.setString(3, item.getProductName());
                    itemStmt.setDouble(4, item.getPrice());
                    itemStmt.setInt(5, item.getQuantity());
                    itemStmt.setDouble(6, item.getTotalPrice());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            con.commit();

        } catch (Exception e) {
            System.out.println("Error saving order: " + e.getMessage());
        }
    }

    // Find order by ID
    public Order findById(String orderId) {
        String orderQuery = "SELECT * FROM orders WHERE order_id = ?";
        String itemsQuery = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection con = DataBaseConnection.getConnection()) {
            Order order = null;

            try (PreparedStatement pstmt = con.prepareStatement(orderQuery)) {
                pstmt.setString(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    order = mapOrderRow(rs);
                }
            }

            if (order == null) {
                throw new InvalidOrderException("InvalidOrderException: Order with ID " + orderId + " does not exist.");
            }

            List<CartItem> items = new ArrayList<>();
            try (PreparedStatement pstmt = con.prepareStatement(itemsQuery)) {
                pstmt.setString(1, orderId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    items.add(new CartItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                    ));
                }
            }

            order.getItems().addAll(items);
            return order;

        } catch (InvalidOrderException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Error finding order: " + e.getMessage());
            throw new InvalidOrderException("InvalidOrderException: Order with ID " + orderId + " does not exist.");
        }
    }

    private void attachItems(Connection con, List<Order> orders, List<String> orderIds) throws SQLException {
        String itemsQuery = "SELECT * FROM order_items WHERE order_id = ?";
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            try (PreparedStatement pstmt = con.prepareStatement(itemsQuery)) {
                pstmt.setString(1, orderIds.get(i));
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    order.getItems().add(new CartItem(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                    ));
                }
            }
        }
    }

    // Find orders by customer ID
    public List<Order> findByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String orderQuery = "SELECT * FROM orders WHERE customer_id = ?";
        try (Connection con = DataBaseConnection.getConnection()) {
            List<String> orderIds = new ArrayList<>();
            try (PreparedStatement pstmt = con.prepareStatement(orderQuery)) {
                pstmt.setInt(1, customerId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    orders.add(mapOrderRow(rs));
                    orderIds.add(rs.getString("order_id"));
                }
            }
            attachItems(con, orders, orderIds);
        } catch (Exception e) {
            System.out.println("Error fetching customer orders: " + e.getMessage());
        }
        return orders;
    }

    // Get all orders
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String orderQuery = "SELECT * FROM orders";
        try (Connection con = DataBaseConnection.getConnection()) {
            List<String> orderIds = new ArrayList<>();
            try (PreparedStatement pstmt = con.prepareStatement(orderQuery);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderRow(rs));
                    orderIds.add(rs.getString("order_id"));
                }
            }
            attachItems(con, orders, orderIds);
        } catch (Exception e) {
            System.out.println("Error fetching all orders: " + e.getMessage());
        }
        return orders;
    }

    // Update order status
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        String query = "UPDATE orders SET order_status = ?, updated_at = NOW() WHERE order_id = ?";
        try (Connection con = DataBaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, orderId);
            pstmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error updating order status: " + e.getMessage());
        }
    }

    // Helper to map order row (without items)
    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Timestamp oTs = null, uTs = null;
        try { oTs = rs.getTimestamp("order_date"); } catch (Exception ignored) {}
        try { uTs = rs.getTimestamp("updated_at"); } catch (Exception ignored) {}
        Order order = new Order(
            rs.getString("order_id"), rs.getInt("customer_id"), new ArrayList<>(),
            rs.getDouble("total_amount"),
            oTs != null ? oTs.toLocalDateTime() : null,
            uTs != null ? uTs.toLocalDateTime() : null
        );
        order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status")));
        return order;
    }
}
