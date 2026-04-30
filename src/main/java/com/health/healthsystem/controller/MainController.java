package com.health.healthsystem.controller;

import com.health.healthsystem.model.*;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.service.ClinicService;
import com.health.healthsystem.util.InputValidator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainController {
    private ClinicService service;
    private User currentUser;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;

    @FXML private Tab tabRegisterPatient, tabRegisterDoctor, tabViewDoctors, tabBookAppointmentAdmin,
            tabViewAllAppointments, tabAddRecord, tabViewRecordsAdmin, tabPatientBook,
            tabPatientAppointments, tabPatientRecords, tabDoctorSchedule, tabDoctorAddRecord, tabDoctorViewRecords;

    @FXML private TextField patientIdField, patientNameField, doctorIdField, doctorNameField, doctorSpecField;
    @FXML private Label patientStatusLabel, doctorStatusLabel, apptStatusLabel, recordStatusLabel, patientApptStatusLabel, doctorRecordStatusLabel;
    @FXML private TextArea doctorsListArea, allAppointmentsArea, recordDataField, recordsListArea, patientAppointmentsArea, patientRecordsArea,
            doctorScheduleArea, doctorRecordDataField, doctorPatientRecordsArea;

    // Admin Appointment Selectors
    @FXML private ComboBox<User> adminApptPatientCombo, adminApptDoctorCombo;
    @FXML private DatePicker adminApptDatePicker;
    @FXML private ComboBox<String> adminApptHourCombo, adminApptMinuteCombo;

    // Patient Appointment Selectors
    @FXML private ComboBox<User> patientDoctorCombo;
    @FXML private DatePicker patientApptDatePicker;
    @FXML private ComboBox<String> patientApptHourCombo, patientApptMinuteCombo;
    @FXML private ComboBox<String> patientSpecialtyFilter;

    // Records Selectors
    @FXML private ComboBox<User> adminRecordPatientCombo, adminViewRecordPatientCombo,
            doctorPatientRecordCombo, doctorViewPatientCombo;

    /**
     * Entry point for the controller.
     */
    public void initSession(User user, ClinicService clinicService) {
        this.currentUser = user;
        this.service = clinicService;
        welcomeLabel.setText("Session: " + user.getFullName() + " [" + user.getRole() + "]");

        initTimeDropdowns();
        applyRolePermissions(user.getRole());
        setupSelectionData();
    }

    /**
     * Initializes the HH:MM dropdowns for a professional booking experience.
     */
    private void initTimeDropdowns() {
        List<String> hours = new ArrayList<>();
        for (int i = 8; i <= 18; i++) hours.add(String.format("%02d", i)); // 8 AM to 6 PM

        List<String> minutes = List.of("00", "15", "30", "45");

        ObservableList<String> hList = FXCollections.observableArrayList(hours);
        ObservableList<String> mList = FXCollections.observableArrayList(minutes);

        if (adminApptHourCombo != null) {
            adminApptHourCombo.setItems(hList);
            adminApptMinuteCombo.setItems(mList);
        }
        if (patientApptHourCombo != null) {
            patientApptHourCombo.setItems(hList);
            patientApptMinuteCombo.setItems(mList);
        }
    }

    private void applyRolePermissions(Role role) {
        mainTabPane.getTabs().clear();
        if (role == Role.ADMIN) {
            mainTabPane.getTabs().addAll(tabRegisterPatient, tabRegisterDoctor, tabBookAppointmentAdmin, tabViewAllAppointments, tabAddRecord, tabViewRecordsAdmin, tabViewDoctors);
        } else if (role == Role.PATIENT) {
            mainTabPane.getTabs().addAll(tabPatientBook, tabPatientAppointments, tabPatientRecords, tabViewDoctors);
        } else if (role == Role.DOCTOR) {
            mainTabPane.getTabs().addAll(tabDoctorSchedule, tabDoctorAddRecord, tabDoctorViewRecords, tabViewDoctors);
        }
    }

    private void setupSelectionData() {
        List<User> doctors = service.getAllDoctors();
        List<User> patients = service.getAllPatients();

        if (adminApptPatientCombo != null) adminApptPatientCombo.setItems(FXCollections.observableArrayList(patients));
        if (adminApptDoctorCombo != null) adminApptDoctorCombo.setItems(FXCollections.observableArrayList(doctors));
        if (adminRecordPatientCombo != null) adminRecordPatientCombo.setItems(FXCollections.observableArrayList(patients));
        if (adminViewRecordPatientCombo != null) adminViewRecordPatientCombo.setItems(FXCollections.observableArrayList(patients));
        if (doctorPatientRecordCombo != null) doctorPatientRecordCombo.setItems(FXCollections.observableArrayList(patients));
        if (doctorViewPatientCombo != null) doctorViewPatientCombo.setItems(FXCollections.observableArrayList(patients));

        if (patientSpecialtyFilter != null) {
            patientSpecialtyFilter.setItems(FXCollections.observableArrayList(service.getUniqueSpecialties()));
            patientSpecialtyFilter.setOnAction(e -> {
                String spec = patientSpecialtyFilter.getValue();
                if (spec != null) {
                    patientDoctorCombo.setItems(FXCollections.observableArrayList(
                            doctors.stream().filter(d -> spec.equals(d.getSpecialization())).toList()
                    ));
                }
            });
        }
    }

    @FXML private void handleRegisterPatient() {
        try {
            String id = patientIdField.getText();
            String name = patientNameField.getText();
            if (id.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("Fields cannot be empty.");

            service.registerUser(new User(id, "pass", Role.PATIENT, name, LocalDate.now(), User.Gender.OTHER, null));
            patientStatusLabel.setText("Patient Registered.");
            patientIdField.clear(); patientNameField.clear();
            setupSelectionData();
        } catch (Exception e) { patientStatusLabel.setText(e.getMessage()); }
    }

    @FXML private void handleRegisterDoctor() {
        try {
            String id = doctorIdField.getText();
            String name = doctorNameField.getText();
            String spec = doctorSpecField.getText();
            if (id.isEmpty() || name.isEmpty() || spec.isEmpty()) throw new IllegalArgumentException("Fields cannot be empty.");

            service.registerUser(new User(id, "pass", Role.DOCTOR, name, LocalDate.now(), User.Gender.OTHER, spec));
            doctorStatusLabel.setText("Doctor Registered.");
            doctorIdField.clear(); doctorNameField.clear(); doctorSpecField.clear();
            setupSelectionData();
        } catch (Exception e) { doctorStatusLabel.setText(e.getMessage()); }
    }

    @FXML private void handleBookAppointment() {
        try {
            User patient = adminApptPatientCombo.getValue();
            User doctor = adminApptDoctorCombo.getValue();
            LocalDate date = adminApptDatePicker.getValue();
            String h = adminApptHourCombo.getValue();
            String m = adminApptMinuteCombo.getValue();

            if (patient == null || doctor == null || date == null || h == null || m == null) {
                apptStatusLabel.setText("Error: Missing appointment details.");
                return;
            }

            LocalDateTime ldt = LocalDateTime.of(date, LocalTime.of(Integer.parseInt(h), Integer.parseInt(m)));
            service.bookAppointment(patient, doctor, ldt);
            apptStatusLabel.setText("Booked for " + ldt.format(DT_FORMAT));
            apptStatusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            apptStatusLabel.setText(e.getMessage());
            apptStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML private void handlePatientBookAppointment() {
        try {
            User doctor = patientDoctorCombo.getValue();
            LocalDate date = patientApptDatePicker.getValue();
            String h = patientApptHourCombo.getValue();
            String m = patientApptMinuteCombo.getValue();

            if (doctor == null || date == null || h == null || m == null) {
                patientApptStatusLabel.setText("Error: Missing selections.");
                return;
            }

            LocalDateTime ldt = LocalDateTime.of(date, LocalTime.of(Integer.parseInt(h), Integer.parseInt(m)));
            service.bookAppointment(currentUser, doctor, ldt);
            patientApptStatusLabel.setText("Booked with " + doctor.getFullName());
            patientApptStatusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            patientApptStatusLabel.setText(e.getMessage());
            patientApptStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML private void handleAddRecord() {
        try {
            User patient = adminRecordPatientCombo.getValue();
            String data = recordDataField.getText();
            if (patient == null || data.isBlank()) throw new IllegalArgumentException("Selection or data missing.");

            service.addMedicalRecord(patient, new MedicalRecords<>(UUID.randomUUID().toString().substring(0, 5), data));
            recordStatusLabel.setText("Medical Record Saved.");
            recordDataField.clear();
        } catch (Exception e) { recordStatusLabel.setText(e.getMessage()); }
    }

    @FXML private void handleDoctorAddRecord() {
        try {
            User patient = doctorPatientRecordCombo.getValue();
            String data = doctorRecordDataField.getText();
            if (patient == null || data.isBlank()) throw new IllegalArgumentException("No patient selected or empty note.");

            service.addMedicalRecord(patient, new MedicalRecords<>(UUID.randomUUID().toString().substring(0, 5), data));
            doctorRecordStatusLabel.setText("Note added to " + patient.getFullName());
            doctorRecordDataField.clear();
        } catch (Exception e) { doctorRecordStatusLabel.setText(e.getMessage()); }
    }

    @FXML private void handleViewRecords() {
        User p = adminViewRecordPatientCombo.getValue();
        if (p != null) renderRecords(p.getMedicalRecords(), recordsListArea);
    }

    @FXML private void handleDoctorViewRecords() {
        User p = doctorViewPatientCombo.getValue();
        if (p != null) renderRecords(p.getMedicalRecords(), doctorPatientRecordsArea);
    }

    @FXML private void handleViewAllAppointments() { renderAppointments(service.getAllAppointments(), allAppointmentsArea); }
    @FXML private void handleViewPatientAppointments() { renderAppointments(service.getAppointmentsByPatient(currentUser.getUsername()), patientAppointmentsArea); }
    @FXML private void handleViewPatientRecords() { renderRecords(currentUser.getMedicalRecords(), patientRecordsArea); }
    @FXML private void handleViewDoctorSchedule() { renderAppointments(service.getAppointmentsByDoctor(currentUser.getUsername()), doctorScheduleArea); }

    @FXML private void handleViewDoctors() {
        StringBuilder sb = new StringBuilder();
        service.getAllDoctors().forEach(d -> sb.append(d.getFullName()).append(" (").append(d.getSpecialization()).append(")\n"));
        doctorsListArea.setText(sb.isEmpty() ? "No doctors available." : sb.toString());
    }

    private void renderAppointments(List<Appointment> appts, TextArea area) {
        StringBuilder sb = new StringBuilder();
        appts.forEach(a -> sb.append(a.getDateTime().format(DT_FORMAT)).append(" | ").append(a.getPatient().getFullName()).append(" -> ").append(a.getDoctor().getFullName()).append("\n"));
        area.setText(sb.isEmpty() ? "No appointments found." : sb.toString());
    }

    private void renderRecords(List<MedicalRecords<?>> records, TextArea area) {
        StringBuilder sb = new StringBuilder();
        records.forEach(r -> sb.append("[").append(r.getCreatedAt().format(DT_FORMAT)).append("] ").append(r.getData()).append("\n"));
        area.setText(sb.isEmpty() ? "No records found." : sb.toString());
    }

    @FXML private void handleLogout() {
        try {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/health/healthsystem/auth-view.fxml"));
            stage.setScene(new Scene(loader.load(), 480, 440));
        } catch (IOException ignored) {}
    }
}