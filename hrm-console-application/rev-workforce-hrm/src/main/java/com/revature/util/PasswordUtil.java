package com.revature.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


//  Utility class for password hashing and verification.
//  Supports both Salted Base64 (New) and Simple Hex (Seed Data).

public class PasswordUtil {

    private static final int SALT_LENGTH = 16;
    private static final String HASH_ALGORITHM = "SHA-256";


//      Hash a password with a random salt  ||  this for NEW users or PASSWORD UPDATES.

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            String saltStr = Base64.getEncoder().encodeToString(salt);
            String hashStr = Base64.getEncoder().encodeToString(hashedPassword);

            return saltStr + ":" + hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }


//     Verify a password against a stored hash  ||  Supports both Salted (Base64) and Hex (Legacy/Seed).

    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || password == null) return false;

        try {
            if (storedHash.contains(":")) {
                String[] parts = storedHash.split(":");
                if (parts.length != 2) return false;

                byte[] salt = Base64.getDecoder().decode(parts[0]);
                byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

                MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
                md.update(salt);
                byte[] inputHash = md.digest(password.getBytes());

                return MessageDigest.isEqual(expectedHash, inputHash);
            }

//             Support for SQL Seed Data  ||  SHA-256 Hex for 'admin123' etc.

            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(storedHash);

        } catch (Exception e) {
            return false;
        }
    }


//      Generate a random temporary password

    public static String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}


