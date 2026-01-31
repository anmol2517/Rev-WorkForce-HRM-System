package com.revature.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;


//   Utility class for console input/output operations.
//   Provides helper methods for reading validated input from console.

public class ConsoleUtil {
    
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";
    

//   Print a header/title with decoration

    public static void printHeader(String title) {
        int width = 60;
        String border = "=".repeat(width);
        System.out.println("\n" + CYAN + BOLD + border + RESET);
        int padding = (width - title.length()) / 2;
        System.out.println(CYAN + BOLD + " ".repeat(padding) + title + RESET);
        System.out.println(CYAN + BOLD + border + RESET + "\n");
    }

    public static void printSubHeader(String title) {
        System.out.println("\n" + BLUE + BOLD + "--- " + title + " ---" + RESET + "\n");
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[SUCCESS] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "[WARNING] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(CYAN + "[INFO] " + message + RESET);
    }

    public static void printMenu(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        System.out.println("  0. Back/Exit");
        System.out.println();
    }

    public static String readString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    public static String readRequiredString(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (!ValidationUtil.isNullOrEmpty(input)) {
                return input;
            }
            printError("This field is required. Please try again.");
        }
    }

    public static String readOptionalString(String prompt) {
        String input = readString(prompt + " (optional)");
        return input.isEmpty() ? null : input;
    }

    public static int readInt(String prompt) {
        while (true) {
            String input = readString(prompt);
            Integer value = ValidationUtil.parseInteger(input);
            if (value != null) {
                return value;
            }
            printError("Please enter a valid number.");
        }
    }

    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt + " (" + min + "-" + max + ")");
            if (value >= min && value <= max) {
                return value;
            }
            printError("Please enter a number between " + min + " and " + max + ".");
        }
    }

    public static int readMenuChoice(int maxOption) {
        return readIntInRange("Enter your choice", 0, maxOption);
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            String input = readString(prompt + " (yyyy-MM-dd)");
            LocalDate date = ValidationUtil.parseDate(input);
            if (date != null) {
                return date;
            }
            printError("Please enter a valid date in format yyyy-MM-dd.");
        }
    }

    public static LocalDate readFutureDate(String prompt) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (!date.isBefore(LocalDate.now())) {
                return date;
            }
            printError("Date cannot be in the past.");
        }
    }

    public static String readEmail(String prompt) {
        while (true) {
            String input = readString(prompt);
            if (ValidationUtil.isValidEmail(input)) {
                return input.toLowerCase();
            }
            printError("Please enter a valid email address.");
        }
    }

    public static String readPhone(String prompt) {
        while (true) {
            String input = readString(prompt + " (optional)");
            if (input.isEmpty()) {
                return null;
            }
            if (ValidationUtil.isValidPhone(input)) {
                return input;
            }
            printError("Please enter a valid phone number (10-15 digits).");
        }
    }
    

    public static String readPassword(String prompt) {
        return readRequiredString(prompt);
    }

    public static String readNewPassword(String prompt) {
        while (true) {
            String password = readRequiredString(prompt);
            String error = ValidationUtil.validatePassword(password);
            if (error == null) {
                return password;
            }
            printError(error);
        }
    }

    public static String readPasswordWithConfirmation(String prompt) {
        while (true) {
            String password = readNewPassword(prompt);
            String confirm = readRequiredString("Confirm " + prompt.toLowerCase());
            if (password.equals(confirm)) {
                return password;
            }
            printError("Passwords do not match. Please try again.");
        }
    }

    public static String readRole(String prompt) {
        System.out.println("Available roles:");
        System.out.println("  1. ADMIN");
        System.out.println("  2. MANAGER");
        System.out.println("  3. EMPLOYEE");
        
        int choice = readIntInRange(prompt, 1, 3);
        switch (choice) {
            case 1: return "ADMIN";
            case 2: return "MANAGER";
            case 3: return "EMPLOYEE";
            default: return "EMPLOYEE";
        }
    }

    public static boolean confirm(String message) {
        while (true) {
            String input = readString(message + " (y/n)").toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            printError("Please enter 'y' for yes or 'n' for no.");
        }
    }

    public static void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static void clearScreen() {
        System.out.print("\n".repeat(2));
    }

    public static void printTableRow(String format, Object... values) {
        System.out.printf(format + "%n", values);
    }

    public static void printLine() {
        System.out.println("-".repeat(60));
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(DATE_FORMATTER);
    }

    public static String formatStatus(String status) {
        if (status == null) return "N/A";
        
        switch (status.toUpperCase()) {
            case "APPROVED":
            case "COMPLETED":
            case "ACTIVE":
                return GREEN + status + RESET;
            case "PENDING":
            case "IN_PROGRESS":
                return YELLOW + status + RESET;
            case "REJECTED":
            case "CANCELLED":
            case "INACTIVE":
                return RED + status + RESET;
            default:
                return status;
        }
    }
    

//      Close scanner

    public static void close() {
        scanner.close();
    }
}

