package com.ecom.main;

import java.util.Scanner;

/**
 * Shared console input helpers used by all menu classes.
 */
public class ConsoleHelper {

    static Scanner sc = new Scanner(System.in);

    public static void setScanner(Scanner scanner) {
        sc = scanner;
    }

    /** Reads a validated integer from the console. */
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid integer.");
            }
        }
    }

    /** Reads a validated double from the console. */
    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    /** Reads a non-empty string from the console. */
    public static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /** Reads a required (non-blank) string, re-prompts if blank. */
    public static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("  This field cannot be blank. Please try again.");
        }
    }

    /** Reads a password (visible input — no masking in console). */
    public static String readPassword(String prompt) {
        return readRequired(prompt);
    }
}
