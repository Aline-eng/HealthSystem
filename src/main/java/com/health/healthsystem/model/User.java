package com.health.healthsystem.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {
    public enum Role { ADMIN, DOCTOR, PATIENT }
    public enum Gender { MALE, FEMALE, OTHER }

    private final String username;
    private final String hashedPassword;
    private final Role role;

    // Identification Fields
    private final String fullName;
    private final LocalDate birthDate;
    private final Gender gender;

    // Role-specific Fields
    private final String specialization; // Only for Doctors
    private final List<MedicalRecords<?>> medicalRecords; // Only for Patients

    public User(String username, String hashedPassword, Role role, String fullName, LocalDate birthDate, Gender gender, String specialization) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.specialization = specialization;
        this.medicalRecords = new ArrayList<>();
    }

    // Getters
    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    public Role getRole() { return role; }
    public String getFullName() { return fullName; }
    public LocalDate getBirthDate() { return birthDate; }
    public Gender getGender() { return gender; }
    public String getSpecialization() { return specialization; }
    public List<MedicalRecords<?>> getMedicalRecords() { return medicalRecords; }

    // Logic Helpers
    public void addMedicalRecord(MedicalRecords<?> record) {
        if (this.role != Role.PATIENT) {
            throw new IllegalStateException("Only users with PATIENT role can have medical records.");
        }
        this.medicalRecords.add(record);
    }

    @Override
    public String toString() {
        return fullName + (specialization != null ? " (" + specialization + ")" : "");
    }
}