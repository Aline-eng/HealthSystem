package com.health.healthsystem.util;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]+$");
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern SPECIALIZATION_PATTERN = Pattern.compile("^[a-zA-Z\\s&-]+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    // Min 8 chars, at least one letter and one digit
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    public static String validateName(String name, String fieldName) {
        String trimmed = validateText(name, fieldName);
        if (!NAME_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(fieldName + " must contain only letters, spaces, periods, hyphens, and apostrophes.");
        return trimmed;
    }

    public static String validateId(String id, String fieldName) {
        String trimmed = validateText(id, fieldName);
        if (!ID_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(fieldName + " must contain only letters, numbers, hyphens, and underscores.");
        return trimmed;
    }

    public static String validateSpecialization(String specialization, String fieldName) {
        String trimmed = validateText(specialization, fieldName);
        if (!SPECIALIZATION_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException(fieldName + " must contain only letters, spaces, ampersands, and hyphens.");
        return trimmed;
    }

    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("Username cannot be empty.");
        String trimmed = username.trim();
        if (!USERNAME_PATTERN.matcher(trimmed).matches())
            throw new IllegalArgumentException("Username must be 3-20 characters: letters, numbers, or underscores only.");
        return trimmed;
    }

    public static String validatePassword(String password) {
        if (password == null || password.isEmpty())
            throw new IllegalArgumentException("Password cannot be empty.");
        if (!PASSWORD_PATTERN.matcher(password).matches())
            throw new IllegalArgumentException("Password must be at least 8 characters and contain at least one letter and one digit.");
        return password;
    }

    public static String validateText(String text, String fieldName) {
        if (text == null || text.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        return text.trim();
    }
}
