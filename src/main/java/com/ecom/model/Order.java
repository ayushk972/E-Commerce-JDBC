package com.ecom.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Order {

    private String        orderId;
    private int           customerId;
    private List<CartItem> items;
    private double        totalqty;
    private OrderStatus   orderStatus;
    private LocalDateTime orderDate; // Created at
    private LocalDateTime updatedAt;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public Order(String orderId, int customerId, List<CartItem> items, double totalqty) {
        this(orderId, customerId, items, totalqty, LocalDateTime.now(), LocalDateTime.now());
    }

    public Order(String orderId, int customerId, List<CartItem> items, double totalqty,
                 LocalDateTime orderDate, LocalDateTime updatedAt) {
        this.orderId     = orderId;
        this.customerId  = customerId;
        this.items       = items;
        this.totalqty    = totalqty;
        this.orderStatus = OrderStatus.PLACED;
        this.orderDate   = orderDate != null ? orderDate : LocalDateTime.now();
        this.updatedAt   = updatedAt != null ? updatedAt : this.orderDate;
    }

    public String        getOrderId()     { return orderId; }
    public int           getCustomerId()  { return customerId; }
    public List<CartItem> getItems()      { return items; }
    public double        getTotalqty()    { return totalqty; }
    public OrderStatus   getOrderStatus() { return orderStatus; }
    public LocalDateTime getOrderDate()   { return orderDate; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    public void setOrderStatus(OrderStatus orderStatus) { this.orderStatus = orderStatus; }
    public void setUpdatedAt(LocalDateTime u)           { this.updatedAt = u; }

    // Customer order view: shows when ordered, no update timestamp
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "+--------------------------------------------------+%n" +
            "| Order ID     : %-33s|%n" +
            "| Customer ID  : %-33d|%n" +
            "| Ordered At   : %-33s|%n" +
            "| Status       : %-33s|%n" +
            "| Total Amount : ₹%-32.2f|%n" +
            "+------ Items ----------------------------------------+%n",
            orderId, customerId, orderDate.format(FMT), orderStatus, totalqty
        ));
        for (CartItem item : items) sb.append(item.toString()).append("\n");
        sb.append("+--------------------------------------------------+");
        return sb.toString();
    }

    // Admin order view: shows both Ordered At and Updated At timestamps
    public String toAdminString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
            "+--------------------------------------------------+%n" +
            "| Order ID     : %-33s|%n" +
            "| Customer ID  : %-33d|%n" +
            "| Ordered At   : %-33s|%n" +
            "| Updated At   : %-33s|%n" +
            "| Status       : %-33s|%n" +
            "| Total Amount : ₹%-32.2f|%n" +
            "+------ Items ----------------------------------------+%n",
            orderId, customerId, orderDate.format(FMT), updatedAt.format(FMT), orderStatus, totalqty
        ));
        for (CartItem item : items) sb.append(item.toString()).append("\n");
        sb.append("+--------------------------------------------------+");
        return sb.toString();
    }
}
