package lk.sunrise.dental.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ================================================================
 * SecurityUtil.java
 * Cryptographic Security Utilities
 *
 * SHA-256 password hashing for secure staff authentication.
 * Package : lk.sunrise.dental.util
 * ================================================================
 */
public class SecurityUtil {

    // Prevent instantiation - utility class
    private SecurityUtil() {}

    /**
     * Hash plain text password using SHA-256.
     *
     * @param plainPassword raw password string
     * @return 64-character hex SHA-256 hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(
                    plainPassword.getBytes(StandardCharsets.UTF_8)
            );
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available.", e);
        }
    }

    /**
     * Verify plain password against stored SHA-256 hash.
     *
     * @param plainPassword raw password to check
     * @param storedHash    SHA-256 hash from database
     * @return true if match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        return hashPassword(plainPassword).equalsIgnoreCase(storedHash);
    }

    /**
     * Convert byte array to hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Sanitize input to prevent XSS attacks.
     * Escapes HTML special characters.
     *
     * @param input raw user input
     * @return sanitized safe string
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        return input.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Check if string is null or empty.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Validate Sri Lankan phone number format.
     * Valid formats: 07XXXXXXXX or +947XXXXXXXX
     *
     * @param contact phone number string
     * @return true if valid
     */
    public static boolean isValidContact(String contact) {
        if (isEmpty(contact)) return false;
        return contact.matches("^(\\+94|0)[0-9]{9}$");
    }

    /**
     * Validate email format.
     *
     * @param email email string
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return true; // email is optional
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Run this main() to generate SHA-256 hashes for setup.sql
     * Usage: Right-click → Run Java
     */
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  SHA-256 Hash Generator - Sunrise Dental");
        System.out.println("===========================================");

        String[] passwords = {"admin123", "reception123", "dentist123"};
        for (String pwd : passwords) {
            System.out.println("Password : " + pwd);
            System.out.println("Hash     : " + hashPassword(pwd));
            System.out.println("-------------------------------------------");
        }
    }
}