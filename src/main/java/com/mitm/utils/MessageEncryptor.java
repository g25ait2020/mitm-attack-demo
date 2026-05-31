package com.mitm.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple encryption utility to demonstrate the difference between
 * encrypted and unencrypted communication in MITM attacks
 * 
 * NOTE: This uses basic encryption for educational purposes only.
 * In production, use proper encryption libraries and key management.
 */
public class MessageEncryptor {
    private static final String ALGORITHM = "AES";
    private static final String DEFAULT_KEY = "MITMDemoKey12345"; // 16 bytes for AES-128

    /**
     * Encrypt a message using AES
     */
    public static String encrypt(String plainText, String key) {
        try {
            SecretKeySpec secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "ENCRYPTION", "Encryption failed: " + e.getMessage());
            return plainText; // Return plain text if encryption fails
        }
    }

    /**
     * Encrypt with default key
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, DEFAULT_KEY);
    }

    /**
     * Decrypt a message using AES
     */
    public static String decrypt(String encryptedText, String key) {
        try {
            SecretKeySpec secretKey = generateKey(key);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "ENCRYPTION", "Decryption failed: " + e.getMessage());
            return encryptedText; // Return encrypted text if decryption fails
        }
    }

    /**
     * Decrypt with default key
     */
    public static String decrypt(String encryptedText) {
        return decrypt(encryptedText, DEFAULT_KEY);
    }

    /**
     * Generate a secret key from a string
     */
    private static SecretKeySpec generateKey(String key) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
        // Use only first 128 bits (16 bytes) for AES-128
        byte[] aesKey = new byte[16];
        System.arraycopy(keyBytes, 0, aesKey, 0, 16);
        return new SecretKeySpec(aesKey, ALGORITHM);
    }

    /**
     * Simple Base64 encoding (not encryption, just obfuscation)
     */
    public static String encodeBase64(String text) {
        return Base64.encodeBase64String(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Simple Base64 decoding
     */
    public static String decodeBase64(String encodedText) {
        try {
            return new String(Base64.decodeBase64(encodedText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encodedText;
        }
    }

    /**
     * Hash a password (for demonstration purposes)
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.log(Logger.LogLevel.ERROR, "ENCRYPTION", "Hashing failed: " + e.getMessage());
            return password;
        }
    }

    /**
     * Check if text appears to be encrypted (Base64 format check)
     */
    public static boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // Simple check: Base64 strings only contain A-Z, a-z, 0-9, +, /, =
        return text.matches("^[A-Za-z0-9+/=]+$") && text.length() % 4 == 0;
    }

    /**
     * Demonstrate encryption strength by attempting to "crack" it
     * (For educational purposes - shows why encryption is important)
     */
    public static String demonstrateCracking(String encryptedText) {
        return "Cannot decrypt without key - This demonstrates why encryption protects against MITM attacks!";
    }
}
