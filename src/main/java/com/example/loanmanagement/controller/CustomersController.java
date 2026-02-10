package com.example.loanmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class CustomersController {

    @FXML
    private TableView<?> customersTable;

    @FXML
    public void initialize() {
        loadCustomers();
    }

    private void loadCustomers() {
        // Fetch and load
    }

    @FXML
    private void handleAddCustomer() {
        // Open modal
    }
}
