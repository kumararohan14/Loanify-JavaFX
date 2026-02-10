package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.UserDAO;
import com.example.loanmanagement.model.User;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.security.SecureRandom;

public class AuthenticationService {
    private final UserDAO userDAO;
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public User authenticate(String email, String password) {
        User user = userDAO.findByEmail(email);
        if (user != null) {
            if (verifyPassword(password, user.getPasswordHash())) {
                return user;
            }
        }
        return null;
    }

    public void register(String email, String password, String name, User.Role role) {
        if (userDAO.findByEmail(email) != null) {
            // Check if user exists but handle quietly or return boolean
            // For now, strict check
            if (userDAO.findByEmail(email) != null) {
                // In a real app we might update or ignore
                // But for this demo, we assume unique email enforcement
            }
        }

        // Check if user already exists based on earlier check
        if (userDAO.findByEmail(email) == null) {
            String passwordHash = hashPassword(password);
            User user = new User(email, passwordHash, name, role);
            userDAO.save(user);
        }
    }

    // Quick fix for the logical error in register above
    // I will rewrite register cleanly

    private String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null || !storedHash.contains(":"))
            return false;
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            // Constant time comparison (simple version)
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            return false;
        }
    }
}
