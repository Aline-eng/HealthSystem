package com.health.healthsystem.controller;

import com.health.healthsystem.model.User;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.model.User.Gender;
import com.health.healthsystem.service.AuthService;
import com.health.healthsystem.service.ClinicService;
import com.health.healthsystem.service.PersistenceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class AuthController {

    private final PersistenceService persistence = new PersistenceService();
    private final AuthService authService = new AuthService(persistence);

    @FXML private Label statusLabel;


    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;


    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;
    @FXML private TextField regFullName;
    @FXML private DatePicker regBirthDate;
    @FXML private ComboBox<String> regGender;
    @FXML private ComboBox<String> regRole;
    @FXML private TextField regSpecialty;


    @FXML
    private void toggleSpecialtyField() {
        boolean isDoctor = "DOCTOR".equals(regRole.getValue());
        regSpecialty.setVisible(isDoctor);
        regSpecialty.setManaged(isDoctor);
    }

    @FXML
    private void handleLogin() {
        try {
            User user = authService.login(
                    loginUsernameField.getText(),
                    loginPasswordField.getText()
            );
            openMainView(user);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleSignup() {
        try {
            String username = regUsername.getText();
            String password = regPassword.getText();
            String fullName = regFullName.getText();
            LocalDate dob = regBirthDate.getValue();
            String genderStr = regGender.getValue();
            String roleStr = regRole.getValue();
            String specialty = regSpecialty.getText();

            if (roleStr == null) throw new IllegalArgumentException("Please select a role.");
            if (genderStr == null) throw new IllegalArgumentException("Please select a gender.");
            if (dob == null) throw new IllegalArgumentException("Please select a birth date.");

            Role role = Role.valueOf(roleStr);
            Gender gender = Gender.valueOf(genderStr);

            // Using the updated AuthService signup logic (requires passing profile info)
            User user = authService.signup(
                    username, password, role, fullName, dob, gender, specialty
            );

            showSuccess("Account created! Logging you in...");
            openMainView(user);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void openMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/health/healthsystem/main-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 900, 650);
            MainController controller = loader.getController();

            controller.initSession(user, new ClinicService(persistence));

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setTitle("Healthcare System — " + user.getFullName());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #c62828;");
    }
}
