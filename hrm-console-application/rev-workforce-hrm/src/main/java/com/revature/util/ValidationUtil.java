package com.revature.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtil {


//         Regex Patterns

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9]{10,15}$");

    private static final Pattern NAME_PATTERN =
        Pattern.compile("^[A-Za-z\\s'-]{2,50}$");

    private static final String[] VALID_ROLES = {"ADMIN", "MANAGER", "EMPLOYEE"};

    private static final String[] VALID_LEAVE_STATUSES =
        {"PENDING", "APPROVED", "REJECTED", "CANCELLED"};

    private static final String[] VALID_GOAL_STATUSES =
        {"NOT_STARTED", "IN_PROGRESS", "COMPLETED", "ON_HOLD"};

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return true;
        }

        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public static boolean isValidName(String name) {
        if (isNullOrEmpty(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static String validatePassword(String password) {
        if (isNullOrEmpty(password)) {
            return "Password cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (password.length() > 100) {
            return "Password cannot exceed 100 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "Password must contain at least one special character";
        }
        return null;
    }

    public static boolean isValidRole(String role) {
        if (isNullOrEmpty(role)) {
            return false;
        }
        try {
            com.revature.model.Employee.Role.valueOf(role.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidLeaveStatus(String status) {
        if (isNullOrEmpty(status)) {
            return false;
        }
        for (String validStatus : VALID_LEAVE_STATUSES) {
            if (validStatus.equalsIgnoreCase(status.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidGoalStatus(String status) {
        if (isNullOrEmpty(status)) {
            return false;
        }
        for (String validStatus : VALID_GOAL_STATUSES) {
            if (validStatus.equalsIgnoreCase(status.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidInteger(String input) {
        if (isNullOrEmpty(input)) {
            return false;
        }
        try {
            Integer.parseInt(input.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Integer parseInteger(String input) {
        if (isNullOrEmpty(input)) {
            return null;
        }
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }


    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }


    public static boolean isValidPercentage(int percentage) {
        return percentage >= 0 && percentage <= 100;
    }


    public static boolean isPositive(int number) {
        return number > 0;
    }


    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }


    public static String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }
}


