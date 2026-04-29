package com.health.healthsystem.model;

import java.time.LocalDateTime;

public class Appointment {
    private final String id;
    private final Patient patient;
    private final Doctor doctor;
    private final LocalDateTime dateTime;

    public Appointment(String id, Patient patient, Doctor doctor, LocalDateTime dateTime) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Appointment ID cannot be empty.");
        if (patient == null) throw new IllegalArgumentException("Patient cannot be null.");
        if (doctor == null) throw new IllegalArgumentException("Doctor cannot be null.");
        if (dateTime == null) throw new IllegalArgumentException("Appointment date and time cannot be null.");
        this.id = id.trim();
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
    }

    public String getId() { return id; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }
    public LocalDateTime getDateTime() { return dateTime; }
}
