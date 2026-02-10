package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.User;
import com.example.loanmanagement.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField roleField;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label securityStatusLabel;

    @FXML
    public void initialize() {
        UserSession session = UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            User user = session.getUser();
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            roleField.setText(user.getRole().toString());
        }
    }

    @FXML
    private void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String newly = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newly.isEmpty() || confirm.isEmpty()) {
            setStatus("Please fill all fields", true);
            return;
        }

        if (!newly.equals(confirm)) {
            setStatus("New passwords do not match", true);
            return;
        }

        // In a real app, verify current password hash and update DB
        // For now, demo simulation
        setStatus("Password updated successfully (Demo)", false);
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void setStatus(String msg, boolean isError) {
        securityStatusLabel.setText(msg);
        securityStatusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
}
