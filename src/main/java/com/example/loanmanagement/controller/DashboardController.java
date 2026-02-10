package com.example.loanmanagement.controller;

import com.example.loanmanagement.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
            // We need to implement loadView in SceneManager to return Parent
            // Or just use FXMLLoader here.
            // Since views might need specific controllers, standard FXMLLoader is fine.
            // But we need the resource path.
            // Let's assume views are in the same package or we use full path.
            // SceneManager.loadView returns Parent.
            mainLayout.setCenter(SceneManager.loadView(fxml));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
