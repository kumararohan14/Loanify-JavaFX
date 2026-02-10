package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.User;
import com.example.loanmanagement.service.AuthenticationService;
import com.example.loanmanagement.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ToggleButton userTypeAdmin;
    @FXML
    private ToggleButton userTypeStaff;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        userTypeAdmin.setToggleGroup(group);
        userTypeStaff.setToggleGroup(group);
        userTypeAdmin.setSelected(true);

        // Seed admin user if not exists (Hack for demo)
        try {
            authService.register("admin@company.com", "admin123", "Administrator", User.Role.ADMIN);
            authService.register("staff@company.com", "staff123", "Loan Officer", User.Role.STAFF);
        } catch (Exception e) {
            // Already exists
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter email and password");
            return;
        }

        User user = authService.authenticate(email, password);
        if (user != null) {
            // Check role match?
            User.Role selectedRole = userTypeAdmin.isSelected() ? User.Role.ADMIN : User.Role.STAFF;
            if (user.getRole() == selectedRole) {
                // Store user session
                com.example.loanmanagement.util.UserSession.setSession(user);
                SceneManager.switchScene("dashboard.fxml");
            } else {
                showAlert("Error", "Invalid role selected for this user");
            }
        } else {
            showAlert("Error", "Invalid credentials");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
