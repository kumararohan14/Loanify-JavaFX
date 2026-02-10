package com.example.loanmanagement.controller;

import javafx.fxml.FXML;

public class HomeDashboardController {

    @FXML
    public void initialize() {
        // Load data for charts and KPIs
        loadKpiData();
        loadCharts();
        loadRecentLoans();
    }

    private void loadKpiData() {
        // Fetch from Service
    }

    private void loadCharts() {
        // Setup charts
    }

    private void loadRecentLoans() {
        // Fetch recent loans
    }

    @FXML
    private void handleNewLoan() {
        // Navigate
    }

    @FXML
    private void handleAddCustomer() {
        // Navigate
    }
}
