package com.health.healthsystem.controller;

import com.health.healthsystem.model.*;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.service.ClinicService;
import com.health.healthsystem.util.InputValidator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainController {

    private ClinicService service;
    private User currentUser;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Header ────────────────────────────────────────────────────────────────
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    // ── Tab references (for show/hide) ────────────────────────────────────────
    @FXML private Tab tabRegisterPatient;
    @FXML private Tab tabRegisterDoctor;
    @FXML private Tab tabViewDoctors;
    @FXML private Tab tabBookAppointmentAdmin;
    @FXML private Tab tabViewAllAppointments;
    @FXML private Tab tabAddRecord;
    @FXML private Tab tabViewRecordsAdmin;
    @FXML private Tab tabPatientBook;
    @FXML private Tab tabPatientAppointments;
    @FXML private Tab tabPatientRecords;
    @FXML private Tab tabDoctorSchedule;
    @FXML private Tab tabDoctorAddRecord;
    @FXML private Tab tabDoctorViewRecords;

    // ── ADMIN fields ──────────────────────────────────────────────────────────
    @FXML private TextField patientIdField;
    @FXML private TextField patientNameField;
    @FXML private Label patientStatusLabel;

    @FXML private TextField doctorIdField;
    @FXML private TextField doctorNameField;
    @FXML private TextField doctorSpecField;
    @FXML private Label doctorStatusLabel;

    @FXML private TextArea doctorsListArea;

    @FXML private TextField apptPatientIdField;
    @FXML private TextField apptDoctorIdField;
    @FXML private TextField apptDateTimeField;
    @FXML private Label apptStatusLabel;

    @FXML private TextArea allAppointmentsArea;

    @FXML private TextField recordPatientIdField;
    @FXML private TextArea recordDataField;
    @FXML private Label recordStatusLabel;

    @FXML private TextField viewRecordPatientIdField;
    @FXML private TextArea recordsListArea;

    // ── PATIENT fields ────────────────────────────────────────────────────────
    @FXML private TextField patientSelfIdField;
    @FXML private TextField patientApptDoctorIdField;
    @FXML private TextField patientApptDateTimeField;
    @FXML private Label patientApptStatusLabel;

    @FXML private TextArea patientAppointmentsArea;
    @FXML private TextArea patientRecordsArea;

    // ── DOCTOR fields ─────────────────────────────────────────────────────────
    @FXML private TextArea doctorScheduleArea;

    @FXML private TextField doctorRecordPatientIdField;
    @FXML private TextArea doctorRecordDataField;
    @FXML private Label doctorRecordStatusLabel;

    @FXML private TextField doctorViewRecordPatientIdField;
    @FXML private TextArea doctorPatientRecordsArea;

    // ── Session init ──────────────────────────────────────────────────────────

    /**
     * Called by AuthController immediately after the FXML is loaded.
     * Sets up the session and applies role-based tab visibility.
     */
    public void initSession(User user, ClinicService clinicService) {
        this.currentUser = user;
        this.service = clinicService;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "   |   Role: " + user.getRole());
        applyRolePermissions(user.getRole());
    }

    private void applyRolePermissions(Role role) {
        // Start by removing all tabs, then add only the ones for this role
        mainTabPane.getTabs().clear();

        switch (role) {
            case ADMIN -> {
                // Full access: all tabs
                mainTabPane.getTabs().addAll(
                    tabRegisterPatient, tabRegisterDoctor, tabViewDoctors,
                    tabBookAppointmentAdmin, tabViewAllAppointments,
                    tabAddRecord, tabViewRecordsAdmin
                );
            }
            case PATIENT -> {
                // Scoped: view doctors, book own appointment, own appointments, own records
                mainTabPane.getTabs().addAll(
                    tabViewDoctors, tabPatientBook, tabPatientAppointments, tabPatientRecords
                );
                // Auto-fill the patient ID field with their username (their patient ID)
                patientSelfIdField.setText(currentUser.getUsername());
            }
            case DOCTOR -> {
                // Scoped: view doctors, own schedule, add/view patient records
                mainTabPane.getTabs().addAll(
                    tabViewDoctors, tabDoctorSchedule, tabDoctorAddRecord, tabDoctorViewRecords
                );
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/health/healthsystem/auth-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 480, 440);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Healthcare Management System — Login");
            stage.setResizable(false);
            stage.setScene(scene);
        } catch (IOException e) {
            showError(patientStatusLabel, "Logout failed: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ADMIN HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleRegisterPatient() {
        try {
            String id   = InputValidator.validateId(patientIdField.getText(), "Patient ID");
            String name = InputValidator.validateName(patientNameField.getText(), "Patient name");
            service.registerPatient(new Patient(id, name));
            showSuccess(patientStatusLabel, "Patient registered successfully.");
            patientIdField.clear(); patientNameField.clear();
        } catch (Exception e) {
            showError(patientStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleRegisterDoctor() {
        try {
            String id   = InputValidator.validateId(doctorIdField.getText(), "Doctor ID");
            String name = InputValidator.validateName(doctorNameField.getText(), "Doctor name");
            String spec = InputValidator.validateSpecialization(doctorSpecField.getText(), "Specialization");
            service.registerDoctor(new Doctor(id, name, spec));
            showSuccess(doctorStatusLabel, "Doctor registered successfully.");
            doctorIdField.clear(); doctorNameField.clear(); doctorSpecField.clear();
        } catch (Exception e) {
            showError(doctorStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleViewDoctors() {
        List<Doctor> doctors = service.getAllDoctors();
        if (doctors.isEmpty()) { doctorsListArea.setText("No doctors registered."); return; }
        StringBuilder sb = new StringBuilder();
        for (Doctor d : doctors)
            sb.append(d.getId()).append(" | ").append(d.getName())
              .append(" | ").append(d.getSpecialization()).append("\n");
        doctorsListArea.setText(sb.toString());
    }

    @FXML
    private void handleBookAppointment() {
        try {
            String patientId = InputValidator.validateId(apptPatientIdField.getText(), "Patient ID");
            String doctorId  = InputValidator.validateId(apptDoctorIdField.getText(), "Doctor ID");
            String dateInput = InputValidator.validateText(apptDateTimeField.getText(), "Date & Time");

            if (service.getPatientById(patientId) == null) {
                showError(apptStatusLabel, "Patient not found. Register the patient first."); return;
            }
            Doctor doctor = service.getDoctorById(doctorId);
            if (doctor == null) { showError(apptStatusLabel, "Doctor not found."); return; }

            LocalDateTime dateTime = parseDateTime(dateInput, apptStatusLabel);
            if (dateTime == null) return;

            service.bookAppointment("A" + System.currentTimeMillis(), patientId, doctor, dateTime);
            showSuccess(apptStatusLabel, "Appointment booked successfully.");
            apptPatientIdField.clear(); apptDoctorIdField.clear(); apptDateTimeField.clear();
        } catch (Exception e) {
            showError(apptStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleViewAllAppointments() {
        renderAppointments(service.getAllAppointments(), allAppointmentsArea, "No appointments booked.");
    }

    @FXML
    private void handleAddRecord() {
        try {
            String patientId = InputValidator.validateId(recordPatientIdField.getText(), "Patient ID");
            String data      = InputValidator.validateText(recordDataField.getText(), "Record description");
            service.addMedicalRecord(patientId, new MedicalRecords<>("R" + System.currentTimeMillis(), data));
            showSuccess(recordStatusLabel, "Medical record added.");
            recordPatientIdField.clear(); recordDataField.clear();
        } catch (Exception e) {
            showError(recordStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleViewRecords() {
        try {
            String patientId = InputValidator.validateId(viewRecordPatientIdField.getText(), "Patient ID");
            renderRecords(service.getPatientRecords(patientId), recordsListArea);
        } catch (Exception e) {
            recordsListArea.setText("Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PATIENT HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handlePatientBookAppointment() {
        try {
            // Patient ID is always their own username — already shown read-only
            String patientId = currentUser.getUsername();
            String doctorId  = InputValidator.validateId(patientApptDoctorIdField.getText(), "Doctor ID");
            String dateInput = InputValidator.validateText(patientApptDateTimeField.getText(), "Date & Time");

            if (service.getPatientById(patientId) == null) {
                showError(patientApptStatusLabel,
                    "Your patient profile was not found. Ask an admin to register you with ID: " + patientId);
                return;
            }
            Doctor doctor = service.getDoctorById(doctorId);
            if (doctor == null) { showError(patientApptStatusLabel, "Doctor not found."); return; }

            LocalDateTime dateTime = parseDateTime(dateInput, patientApptStatusLabel);
            if (dateTime == null) return;

            service.bookAppointment("A" + System.currentTimeMillis(), patientId, doctor, dateTime);
            showSuccess(patientApptStatusLabel, "Appointment booked successfully.");
            patientApptDoctorIdField.clear(); patientApptDateTimeField.clear();
        } catch (Exception e) {
            showError(patientApptStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleViewPatientAppointments() {
        renderAppointments(
            service.getAppointmentsByPatient(currentUser.getUsername()),
            patientAppointmentsArea,
            "You have no appointments booked."
        );
    }

    @FXML
    private void handleViewPatientRecords() {
        try {
            renderRecords(service.getPatientRecords(currentUser.getUsername()), patientRecordsArea);
        } catch (Exception e) {
            patientRecordsArea.setText("No records found. Ask your doctor or admin to add records.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DOCTOR HANDLERS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleViewDoctorSchedule() {
        // Doctor's ID is their username
        renderAppointments(
            service.getAppointmentsByDoctor(currentUser.getUsername()),
            doctorScheduleArea,
            "You have no appointments scheduled."
        );
    }

    @FXML
    private void handleDoctorAddRecord() {
        try {
            String patientId = InputValidator.validateId(doctorRecordPatientIdField.getText(), "Patient ID");
            String data      = InputValidator.validateText(doctorRecordDataField.getText(), "Record description");
            service.addMedicalRecord(patientId, new MedicalRecords<>("R" + System.currentTimeMillis(), data));
            showSuccess(doctorRecordStatusLabel, "Medical record added for patient " + patientId + ".");
            doctorRecordPatientIdField.clear(); doctorRecordDataField.clear();
        } catch (Exception e) {
            showError(doctorRecordStatusLabel, e.getMessage());
        }
    }

    @FXML
    private void handleDoctorViewRecords() {
        try {
            String patientId = InputValidator.validateId(doctorViewRecordPatientIdField.getText(), "Patient ID");
            renderRecords(service.getPatientRecords(patientId), doctorPatientRecordsArea);
        } catch (Exception e) {
            doctorPatientRecordsArea.setText("Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SHARED HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private LocalDateTime parseDateTime(String input, Label errorLabel) {
        try {
            return LocalDateTime.parse(input, DT_FORMAT);
        } catch (DateTimeParseException e) {
            showError(errorLabel, "Invalid date format. Use: yyyy-MM-dd HH:mm  (e.g. 2025-12-01 14:00)");
            return null;
        }
    }

    private void renderAppointments(List<Appointment> appointments, TextArea area, String emptyMsg) {
        if (appointments.isEmpty()) { area.setText(emptyMsg); return; }
        StringBuilder sb = new StringBuilder();
        for (Appointment a : appointments)
            sb.append(a.getId())
              .append(" | Patient: ").append(a.getPatient().getName())
              .append(" (").append(a.getPatient().getId()).append(")")
              .append(" | Doctor: ").append(a.getDoctor().getName())
              .append(" | Date: ").append(a.getDateTime().format(DT_FORMAT))
              .append("\n");
        area.setText(sb.toString());
    }

    private void renderRecords(List<? extends MedicalRecords<?>> records, TextArea area) {
        if (records.isEmpty()) { area.setText("No records found."); return; }
        StringBuilder sb = new StringBuilder();
        for (var r : records)
            sb.append("ID: ").append(r.getId())
              .append(" | Data: ").append(r.getData())
              .append(" | Date: ").append(r.getCreatedAt().format(DT_FORMAT))
              .append("\n");
        area.setText(sb.toString());
    }

    private void showSuccess(Label label, String msg) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: #2e7d32;");
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: #c62828;");
    }
}
