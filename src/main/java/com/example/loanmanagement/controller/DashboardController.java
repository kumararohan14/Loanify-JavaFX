package com.example.loanmanagement.controller;

import com.example.loanmanagement.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class DashboardController {

    private static DashboardController instance;

    @FXML
    private BorderPane mainLayout;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label userNameLabel;

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Load User Info
        com.example.loanmanagement.util.UserSession session = com.example.loanmanagement.util.UserSession.getInstance();
        if (session != null && session.getUser() != null) {
            userNameLabel.setText(session.getUser().getName());
            userRoleLabel.setText(session.getUser().getRole().toString());
        }

        // Load default view (Dashboard Home)
        loadView("home_dashboard.fxml");
    }

    @FXML
    private void showDashboard() {
        loadView("home_dashboard.fxml");
    }

    @FXML
    private void showCustomers() {
        loadView("customers.fxml");
    }

    @FXML
    private void showLoans() {
        loadView("loans.fxml");
    }

    @FXML
    private void showLoanApplication() {
        loadView("loan_application.fxml");
    }

    @FXML
    private void showPayments() {
        loadView("payments.fxml");
    }

    @FXML
    private void showReports() {
        loadView("reports.fxml");
    }

    @FXML
    private void showSettings() {
        loadView("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        SceneManager.switchScene("login.fxml");
    }

    public void loadView(String fxml) {
        try {
            mainLayout.setCenter(SceneManager.loadView(fxml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadViewNode(javafx.scene.Node node) {
        mainLayout.setCenter(node);
    }
}
