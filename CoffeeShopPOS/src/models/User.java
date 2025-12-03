package models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private int id;
    private String username;
    private String passwordHash; // Add this field
    private String role;
    private boolean isActive;
    
    // Constructor with password (for creating new users)
    public User(int id, String username, String passwordHash, String role, boolean isActive) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
    }
    
    // Constructor without password (for loading users from database)
    public User(int id, String username, String role, boolean isActive) {
        this(id, username, "", role, isActive);
    }
    
    // Constructor for creating new users
    public User(String username, String password, String role) {
        this(0, username, hashPassword(password), role, true);
    }
    
    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }
    
    // Setters (if needed)
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active) { isActive = active; }
    
    // Password hashing utility
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    // Verify password
    public boolean verifyPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}