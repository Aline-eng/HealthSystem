package com.health.healthsystem.service;

import com.health.healthsystem.model.User;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.model.User.Gender;
import com.health.healthsystem.util.InputValidator;
import com.health.healthsystem.util.PasswordUtil;

import java.time.LocalDate;
import java.util.Map;

public class AuthService {

    private final PersistenceService persistence;
    private final Map<String, User> users;

    public AuthService(PersistenceService persistence) {
        this.persistence = persistence;
        this.users = persistence.loadUsers();
    }

    /**
     * Updated signup to handle full identification and profiling.
     */
    public User signup(String username, String password, Role role,
                       String fullName, LocalDate birthDate, Gender gender, String specialty) {

        String validUsername = InputValidator.validateUsername(username);
        String validPassword = InputValidator.validatePassword(password);

        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Full name is required.");
        if (birthDate == null)
            throw new IllegalArgumentException("Birth date is required.");
        if (gender == null)
            throw new IllegalArgumentException("Gender is required.");

        if (users.containsKey(validUsername.toLowerCase()))
            throw new IllegalArgumentException("Username already taken.");

        // If it's a doctor, we might want to validate specialty too
        String validSpecialty = (role == Role.DOCTOR) ? specialty : null;

        User user = new User(
                validUsername.toLowerCase(),
                PasswordUtil.hash(validPassword),
                role,
                fullName,
                birthDate,
                gender,
                validSpecialty
        );

        users.put(user.getUsername(), user);
        persistence.saveUsers(users);
        return user;
    }

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