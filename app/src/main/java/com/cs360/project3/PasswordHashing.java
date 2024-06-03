package com.cs360.project3;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordHashing {

    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // Length of salt in bytes

    // Method to generate a random salt
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    // Method to hash a password using SHA-256
    public static byte[] hashPassword(String password, byte[] salt) {
        try {
            // Concatenate password and salt
            byte[] passwordAndSalt = concatenateByteArrays(password.getBytes(), salt);

            // Create SHA-256 digest
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return digest.digest(passwordAndSalt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to concatenate two byte arrays
    private static byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
