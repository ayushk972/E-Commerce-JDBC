package com.ecom.exception;

/**
 * Thrown when the application fails to establish a connection to the MySQL database.
 * Wraps the underlying SQLException for cleaner error propagation.
 */
public class DatabaseConnectionException extends RuntimeException {
    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
