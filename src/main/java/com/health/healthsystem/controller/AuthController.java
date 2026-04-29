package com.health.healthsystem.controller;

import com.health.healthsystem.model.User;
import com.health.healthsystem.model.User.Role;
import com.health.healthsystem.service.AuthService;
import com.health.healthsystem.service.ClinicService;
import com.health.healthsystem.service.PersistenceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {

    private final PersistenceService persistence = new PersistenceService();
    private final AuthService authService = new AuthService(persistence);

    // ── Shared ────────────────────────────────────────────────────────────────
    @FXML private TabPane authTabPane;
    @FXML private Label loginStatusLabel;
    @FXML private Label signupStatusLabel;

    // ── Login ─────────────────────────────────────────────────────────────────
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;

    @FXML
    private void handleLogin() {
        try {
            User user = authService.login(
                loginUsernameField.getText(),
                loginPasswordField.getText()
            );
            openMainView(user);
        } catch (Exception e) {
            showError(loginStatusLabel, e.getMessage());
        }
    }

    // ── Signup ────────────────────────────────────────────────────────────────
    @FXML private TextField signupUsernameField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPasswordField;
    @FXML private ComboBox<String> signupRoleCombo;

    @FXML
    private void handleSignup() {
        try {
            String roleStr = signupRoleCombo.getValue();
            if (roleStr == null || roleStr.isBlank()) {
                showError(signupStatusLabel, "Please select a role."); return;
            }
            Role role = Role.valueOf(roleStr.toUpperCase());
            User user = authService.signup(
                signupUsernameField.getText(),
                signupPasswordField.getText(),
                signupConfirmPasswordField.getText(),
                role
            );
            showSuccess(signupStatusLabel, "Account created! Logging you in...");
            openMainView(user);
        } catch (Exception e) {
            showError(signupStatusLabel, e.getMessage());
        }
    }

    // ── Scene switch ──────────────────────────────────────────────────────────
    private void openMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/health/healthsystem/main-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 580);
            MainController controller = loader.getController();
            controller.initSession(user, new ClinicService(user.getUsername(), persistence));

            Stage stage = (Stage) loginUsernameField.getScene().getWindow();
            stage.setTitle("Healthcare System — " + user.getUsername() + " (" + user.getRole() + ")");
            stage.setResizable(true);
            stage.setScene(scene);
        } catch (IOException e) {
            showError(loginStatusLabel, "Failed to load main view: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void showSuccess(Label label, String msg) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: #2e7d32;");
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: #c62828;");
    }
}
