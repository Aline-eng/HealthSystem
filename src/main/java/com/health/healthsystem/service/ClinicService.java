package com.health.healthsystem.service;

import com.health.healthsystem.exception.AppointmentException;
import com.health.healthsystem.model.*;
import com.health.healthsystem.model.User.Role;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ClinicService {

    private final PersistenceService persistence;
    private final Map<String, User> allUsers;
    private final List<Appointment> appointments;

    public ClinicService(PersistenceService persistence) {
        this.persistence = persistence;
        this.allUsers = persistence.loadUsers();
        this.appointments = persistence.loadAllAppointments(allUsers);
    }

    // ── User Management ───────────────────────────────────────────────────────

    /** Registers any user type (Admin, Doctor, Patient) into the system. */
    public void registerUser(User user) {
        if (allUsers.containsKey(user.getUsername())) {
            throw new IllegalArgumentException("User with this username already exists.");
        }
        allUsers.put(user.getUsername(), user);
        persistence.saveUsers(allUsers);
    }

    public User getUserById(String username) {
        return allUsers.get(username.toLowerCase());
    }

    public List<User> getAllDoctors() {
        return allUsers.values().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .collect(Collectors.toList());
    }

    public List<User> getAllPatients() {
        return allUsers.values().stream()
                .filter(u -> u.getRole() == Role.PATIENT)
                .collect(Collectors.toList());
    }

    public List<String> getUniqueSpecialties() {
        return getAllDoctors().stream()
                .map(User::getSpecialization)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ── Clinical Logic ────────────────────────────────────────────────────────

    public void bookAppointment(User patient, User doctor, LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new AppointmentException("Cannot book an appointment in the past.");
        }

        // Check for doctor availability (30-minute buffer)
        boolean hasConflict = appointments.stream()
                .filter(a -> a.getDoctor().getUsername().equals(doctor.getUsername()))
                .anyMatch(a -> Math.abs(java.time.Duration.between(a.getDateTime(), dateTime).toMinutes()) < 30);

        if (hasConflict) {
            throw new AppointmentException("The doctor is already booked at or near this time.");
        }

        Appointment appt = new Appointment(UUID.randomUUID().toString().substring(0, 8), patient, doctor, dateTime);
        appointments.add(appt);
        persistence.saveAppointments(appointments);
    }

    public void addMedicalRecord(User patient, MedicalRecords<?> record) {
        patient.addMedicalRecord(record);
        persistence.saveUsers(allUsers); // Persists the updated user with the new record
    }

    public List<Appointment> getAllAppointments() {
        return Collections.unmodifiableList(appointments);
    }

    public List<Appointment> getAppointmentsByPatient(String username) {
        return appointments.stream()
                .filter(a -> a.getPatient().getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByDoctor(String username) {
        return appointments.stream()
                .filter(a -> a.getDoctor().getUsername().equalsIgnoreCase(username))
                .collect(Collectors.toList());
    }
}