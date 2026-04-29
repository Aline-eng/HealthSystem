package com.health.healthsystem.service;

import com.health.healthsystem.exception.AppointmentException;
import com.health.healthsystem.exception.PatientNotFoundException;
import com.health.healthsystem.model.*;
import com.health.healthsystem.repository.PatientRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClinicService {

    private static final int APPOINTMENT_BUFFER_MINUTES = 60;
    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String username;
    private final PersistenceService persistence;
    private final PatientRepository patientRepo;
    private final List<Doctor> doctors;
    private final List<Appointment> appointments;

    public ClinicService(String username, PersistenceService persistence) {
        this.username = username;
        this.persistence = persistence;
        this.patientRepo = new PatientRepository();
        this.doctors = new ArrayList<>();
        this.appointments = new ArrayList<>();
        loadAll();
    }

    // ── Load from files on login ──────────────────────────────────────────────

    private void loadAll() {
        // Patients
        for (String[] row : persistence.loadPatients(username)) {
            if (row.length >= 2) {
                try { patientRepo.addPatient(new Patient(unescape(row[0]), unescape(row[1]))); }
                catch (Exception ignored) {}
            }
        }
        // Doctors
        for (String[] row : persistence.loadDoctors(username)) {
            if (row.length >= 3) {
                try { doctors.add(new Doctor(unescape(row[0]), unescape(row[1]), unescape(row[2]))); }
                catch (Exception ignored) {}
            }
        }
        // Records (must load before appointments so patients exist)
        for (String[] row : persistence.loadRecords(username)) {
            if (row.length >= 4) {
                try {
                    Patient p = patientRepo.getPatient(unescape(row[1]));
                    if (p != null) {
                        LocalDateTime createdAt = LocalDateTime.parse(unescape(row[3]), DT);
                        p.addMedicalRecord(new MedicalRecords<>(unescape(row[0]), unescape(row[2]), createdAt));
                    }
                } catch (Exception ignored) {}
            }
        }
        // Appointments
        for (String[] row : persistence.loadAppointments(username)) {
            if (row.length >= 4) {
                try {
                    Patient p = patientRepo.getPatient(unescape(row[1]));
                    Doctor d = getDoctorById(unescape(row[2]));
                    LocalDateTime dt = LocalDateTime.parse(unescape(row[3]), DT);
                    if (p != null && d != null)
                        appointments.add(new Appointment(unescape(row[0]), p, d, dt));
                } catch (Exception ignored) {}
            }
        }
    }

    // ── Register Patient ──────────────────────────────────────────────────────

    public void registerPatient(Patient patient) {
        patientRepo.addPatient(patient);
        persistence.savePatients(username, getAllPatients());
    }

    // ── Register Doctor ───────────────────────────────────────────────────────

    public void registerDoctor(Doctor doctor) {
        if (doctor == null) throw new IllegalArgumentException("Doctor cannot be null.");
        if (getDoctorById(doctor.getId()) != null) throw new IllegalArgumentException("Doctor ID already exists.");
        doctors.add(doctor);
        persistence.saveDoctors(username, doctors);
    }

    // ── Book Appointment ──────────────────────────────────────────────────────

    public void bookAppointment(String appointmentId, String patientId, Doctor doctor, LocalDateTime dateTime) {
        if (appointmentId == null || appointmentId.isBlank()) throw new AppointmentException("Appointment ID is required.");
        if (doctor == null) throw new AppointmentException("Doctor is required.");
        if (dateTime == null) throw new AppointmentException("Date and time is required.");

        Patient patient = patientRepo.getPatient(patientId);
        if (patient == null) throw new PatientNotFoundException("Patient not registered.");
        if (dateTime.isBefore(LocalDateTime.now())) throw new AppointmentException("Cannot book an appointment in the past.");

        checkConflict(doctor, dateTime);
        appointments.add(new Appointment(appointmentId.trim(), patient, doctor, dateTime));
        persistence.saveAppointments(username, appointments);
    }

    private void checkConflict(Doctor doctor, LocalDateTime requested) {
        for (Appointment a : appointments) {
            if (a.getDoctor().getId().equals(doctor.getId())) {
                LocalDateTime existing = a.getDateTime();
                if (!requested.isBefore(existing.minusMinutes(APPOINTMENT_BUFFER_MINUTES))
                        && !requested.isAfter(existing.plusMinutes(APPOINTMENT_BUFFER_MINUTES)))
                    throw new AppointmentException(
                        "Doctor is not available at " + requested.format(DT) +
                        ". Conflict with existing appointment at " + existing.format(DT) +
                        ". Minimum gap is " + APPOINTMENT_BUFFER_MINUTES + " minutes.");
            }
        }
    }

    // ── Medical Records ───────────────────────────────────────────────────────

    public <T> void addMedicalRecord(String patientId, MedicalRecords<T> record) {
        Patient patient = patientRepo.getPatient(patientId);
        if (patient == null) throw new PatientNotFoundException("Patient not found.");
        patient.addMedicalRecord(record);
        persistence.saveRecords(username, getAllPatients());
    }

    public List<? extends MedicalRecords<?>> getPatientRecords(String patientId) {
        Patient patient = patientRepo.getPatient(patientId);
        if (patient == null) throw new PatientNotFoundException("Patient not found.");
        return patient.getMedicalRecords();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Doctor getDoctorById(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) return null;
        for (Doctor d : doctors) if (d.getId().equalsIgnoreCase(doctorId.trim())) return d;
        return null;
    }

    /** Returns appointments where the doctor ID matches — used by DOCTOR role. */
    public List<Appointment> getAppointmentsByDoctor(String doctorId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments)
            if (a.getDoctor().getId().equalsIgnoreCase(doctorId)) result.add(a);
        return Collections.unmodifiableList(result);
    }

    /** Returns appointments where the patient ID matches — used by PATIENT role. */
    public List<Appointment> getAppointmentsByPatient(String patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments)
            if (a.getPatient().getId().equalsIgnoreCase(patientId)) result.add(a);
        return Collections.unmodifiableList(result);
    }

    /**
     * Finds a Patient whose ID matches the given username (case-insensitive).
     * When a PATIENT user registers, their patient ID is expected to match their username.
     */
    public Patient getPatientByUsername(String username) {
        return patientRepo.getPatient(username.toLowerCase());
    }

    public Patient getPatientById(String patientId) { return patientRepo.getPatient(patientId); }
    public List<Doctor> getAllDoctors() { return Collections.unmodifiableList(doctors); }
    public List<Appointment> getAllAppointments() { return Collections.unmodifiableList(appointments); }
    public List<Patient> getAllPatients() { return patientRepo.getAllPatients(); }

    private String unescape(String s) { return s.replace("\\,", ","); }
}
