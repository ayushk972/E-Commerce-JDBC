package com.ecom.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ExceptionsTest {

    @Test
    void testAllCustomExceptions() {
        assertEquals("err", new CustomerNotFoundException("err").getMessage());
        assertEquals("err", new DuplicateEmailException("err").getMessage());
        assertEquals("err", new DuplicateProductException("err").getMessage());
        assertEquals("err", new EmptyCartException("err").getMessage());
        assertEquals("err", new InsufficientStockException("err").getMessage());
        assertEquals("err", new InvalidCustomerDataException("err").getMessage());
        assertEquals("err", new InvalidOrderException("err").getMessage());
        assertEquals("err", new InvalidOrderStatusTransitionException("err").getMessage());
        assertEquals("err", new InvalidPriceException("err").getMessage());
        assertEquals("err", new InvalidProductException("err").getMessage());
        assertEquals("err", new InvalidQuantityException("err").getMessage());
        assertEquals("err", new InvalidSearchCriteriaException("err").getMessage());
        assertEquals("err", new OrderAlreadyCancelledException("err").getMessage());
        assertEquals("err", new OrderAlreadyDeliveredException("err").getMessage());
        assertEquals("err", new ProductNotFoundException("err").getMessage());
        assertEquals("err", new UnauthorizedOrderAccessException("err").getMessage());
        assertEquals("err", new CartItemNotFoundException("err").getMessage());

        DatabaseConnectionException dbe = new DatabaseConnectionException("db err");
        assertEquals("db err", dbe.getMessage());
        Throwable cause = new RuntimeException("root cause");
        DatabaseConnectionException dbeWithCause = new DatabaseConnectionException("db err 2", cause);
        assertEquals("db err 2", dbeWithCause.getMessage());
        assertEquals(cause, dbeWithCause.getCause());
    }
}
