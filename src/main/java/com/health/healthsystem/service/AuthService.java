package com.health.healthsystem.service;

import com.health.healthsystem.model.User;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.util.InputValidator;
import com.health.healthsystem.util.PasswordUtil;

import java.util.Map;

public class AuthService {

    private final PersistenceService persistence;
    /** Key-value map: username -> User (loaded from file on startup). */
    private final Map<String, User> users;

    public AuthService(PersistenceService persistence) {
        this.persistence = persistence;
        this.users = persistence.loadUsers();
    }

    /**
     * Registers a new user.
     * @param username  validated username
     * @param password  raw password (will be hashed)
     * @param role      ADMIN or PATIENT
     * @throws IllegalArgumentException if username taken or inputs invalid
     */
    public User signup(String username, String password, String confirmPassword, Role role) {
        String validUsername = InputValidator.validateUsername(username);
        String validPassword = InputValidator.validatePassword(password);

        if (!password.equals(confirmPassword))
            throw new IllegalArgumentException("Passwords do not match.");
        if (users.containsKey(validUsername.toLowerCase()))
            throw new IllegalArgumentException("Username already taken.");

        User user = new User(validUsername.toLowerCase(), PasswordUtil.hash(validPassword), role);
        users.put(user.getUsername(), user);
        persistence.saveUsers(users);
        return user;
    }

    /**
     * Authenticates an existing user.
     * @return the authenticated User
     * @throws IllegalArgumentException if credentials are invalid
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");

        User user = users.get(username.trim().toLowerCase());
        if (user == null || !PasswordUtil.matches(password, user.getHashedPassword()))
            throw new IllegalArgumentException("Invalid username or password.");

        return user;
    }
}
