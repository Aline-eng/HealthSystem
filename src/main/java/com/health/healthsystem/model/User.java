package com.health.healthsystem.model;

public class User {
    public enum Role { ADMIN, DOCTOR, PATIENT }

    private final String username;
    private final String hashedPassword;
    private final Role role;

    public User(String username, String hashedPassword, Role role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public Role getRole() { return role; }
}
