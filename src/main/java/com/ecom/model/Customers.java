package com.ecom.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Customers {
	private int customerId;
	private String name;
	private String email;
	private String password;
	private String mobile;
	private String address;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

	public Customers(int customerId, String name, String email, String password, String mobile, String address) {
		this(customerId, name, email, password, mobile, address, LocalDateTime.now(), LocalDateTime.now());
	}

	public Customers(int customerId, String name, String email, String password, String mobile, String address,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.customerId = customerId;
		this.name = name;
		this.email = email;
		this.password = password;
		this.mobile = mobile;
		this.address = address;
		this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
		this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
	}

	public Customers() {
		super();
		this.customerId = 0;
		this.name = "Guest";
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

    // Getters
	public int getCustomerId() {
		return customerId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getMobile() {
		return mobile;
	}

	public String getAddress() {
		return address;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

    // Setters
	public void setAddress(String address) {
		this.address = address;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setCreatedAt(LocalDateTime c) {
		this.createdAt = c;
	}

	public void setUpdatedAt(LocalDateTime u) {
		this.updatedAt = u;
	}

    // Customer profile view
    @Override
    public String toString() {
        return String.format(
            "+--------------------------------------------------+%n" +
            "| Customer ID  : %-33d|%n" +
            "| Name         : %-33s|%n" +
            "| Email        : %-33s|%n" +
            "| Mobile       : %-33s|%n" +
            "| Address      : %-33s|%n" +
            "+--------------------------------------------------+",
            customerId, name, email, mobile, address
        );
    }

    // Admin customer view: includes Created At and Updated At timestamps
    public String toAdminString() {
        return String.format(
            "+--------------------------------------------------+%n" +
            "| Customer ID  : %-33d|%n" +
            "| Name         : %-33s|%n" +
            "| Email        : %-33s|%n" +
            "| Mobile       : %-33s|%n" +
            "| Address      : %-33s|%n" +
            "| Created At   : %-33s|%n" +
            "| Updated At   : %-33s|%n" +
            "+--------------------------------------------------+",
            customerId, name, email, mobile, address,
            createdAt.format(FMT), updatedAt.format(FMT)
        );
    }
}
