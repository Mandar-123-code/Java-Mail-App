package com.javamail.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil - Handles SHA-256 password hashing.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hashes a plain-text password using SHA-256.
     * @param plainText the raw password
     * @return lowercase hex-encoded SHA-256 hash
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies plain text against a stored hash.
     */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equals(storedHash);
    }
}
