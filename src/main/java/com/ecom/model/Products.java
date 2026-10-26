package com.ecom.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Products {
	private int productId;
	private String productName;
	private String category;
	private double price;
	private int quantity;
	private String brand;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;


	public Products(int productId, String productName, String category, double price, int quantity, String brand) {
		this(productId, productName, category, price, quantity, brand, LocalDateTime.now(), LocalDateTime.now());
	}

	public Products(int productId, String productName, String category, double price, int quantity, String brand,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.productId = productId;
		this.productName = productName;
		this.category = category;
		this.price = price;
		this.quantity = quantity;
		this.brand = brand;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public int getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public String getCategory() {
		return category;
	}

	public double getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getBrand() {
		return brand;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setCreatedAt(LocalDateTime c) {
		this.createdAt = c;
	}

	public void setUpdatedAt(LocalDateTime u) {
		this.updatedAt = u;
	}

    // Customer & Guest view: only product details, no timestamps
    @Override
    public String toString() {
        return String.format(
            "+--------------------------------------------------+%n" +
            "| Product ID   : %-33d|%n" +
            "| Name         : %-33s|%n" +
            "| Category     : %-33s|%n" +
            "| Brand        : %-33s|%n" +
            "| Price        : ₹%-32.2f|%n" +
            "| Quantity     : %-33d|%n" +
            "+--------------------------------------------------+",
            productId, productName, category, brand, price, quantity
        );
    }

    // Admin view: includes Created At and Updated At timestamps
    public String toAdminString() {
        return String.format(
            "+--------------------------------------------------+%n" +
            "| Product ID   : %-33d|%n" +
            "| Name         : %-33s|%n" +
            "| Category     : %-33s|%n" +
            "| Brand        : %-33s|%n" +
            "| Price        : ₹%-32.2f|%n" +
            "| Quantity     : %-33d|%n" +
            "| Created At   : %-33s|%n" +
            "| Updated At   : %-33s|%n" +
            "+--------------------------------------------------+",
            productId, productName, category, brand, price, quantity,
            createdAt, updatedAt
        );
    }
}
