package com.ecom.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ConsoleHelperTest {

    @AfterEach
    void tearDown() {
        ConsoleHelper.setScanner(new Scanner(System.in));
    }

    private void setInput(String data) {
        ConsoleHelper.setScanner(new Scanner(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void testReadIntValidAndInvalid() {
        setInput("abc\n42\n");
        int val = ConsoleHelper.readInt("Enter: ");
        assertEquals(42, val);
    }

    @Test
    void testReadDoubleValidAndInvalid() {
        setInput("not_a_num\n19.99\n");
        double val = ConsoleHelper.readDouble("Enter: ");
        assertEquals(19.99, val, 0.001);
    }

    @Test
    void testReadString() {
        setInput("hello world\n");
        String val = ConsoleHelper.readString("Enter: ");
        assertEquals("hello world", val);
    }

    @Test
    void testReadRequired() {
        setInput("   \n\nactual_value\n");
        String val = ConsoleHelper.readRequired("Enter: ");
        assertEquals("actual_value", val);
    }

    @Test
    void testReadPassword() {
        setInput("secret123\n");
        String val = ConsoleHelper.readPassword("Password: ");
        assertEquals("secret123", val);
    }

    @Test
    void testConstructor() {
        assertNotNull(new ConsoleHelper());
    }
}
