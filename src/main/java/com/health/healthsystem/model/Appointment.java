package com.health.healthsystem.model;

import java.time.LocalDateTime;

public class Appointment {
    private final String id;
    private final User patient; // Unified: uses User instead of Patient class
    private final User doctor;  // Unified: uses User instead of Doctor class
    private final LocalDateTime dateTime;

    public Appointment(String id, User patient, User doctor, LocalDateTime dateTime) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Appointment ID cannot be empty.");
        if (patient == null || patient.getRole() != User.Role.PATIENT)
            throw new IllegalArgumentException("Valid patient is required.");
        if (doctor == null || doctor.getRole() != User.Role.DOCTOR)
            throw new IllegalArgumentException("Valid doctor is required.");
        if (dateTime == null) throw new IllegalArgumentException("Appointment date and time cannot be null.");

        this.id = id.trim();
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
    }

    public String getId() { return id; }
    public User getPatient() { return patient; }
    public User getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
}