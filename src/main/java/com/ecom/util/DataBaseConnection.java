package com.ecom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.ecom.exception.DatabaseConnectionException;

public class DataBaseConnection {

	private static final String url = "jdbc:mysql://localhost:3306/e_commerce";
	private static final String user = "root";
	private static final String password = "0153";

	public static Connection getConnection() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			throw new DatabaseConnectionException("DatabaseConnectionException: Could not connect to the database. "
					+ "Please ensure MySQL is running on localhost:3306. Details: " + e.getMessage(), e);
		}
	}
}
