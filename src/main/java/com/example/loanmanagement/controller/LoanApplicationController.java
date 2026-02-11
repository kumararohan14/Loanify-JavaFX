package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

public class LoanApplicationController {
    @FXML
    private ComboBox<Customer> customerCombo;
    @FXML
    private ComboBox<Loan.LoanType> loanTypeCombo;
    @FXML
    private TextField amountField;
    @FXML
    private TextField interestField;
    @FXML
    private ComboBox<Integer> durationCombo; // Changed to ComboBox
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private Label emiPreviewLabel;
    @FXML
    private Label statusLabel;

    private final LoanService loanService = new LoanService();
    private final CustomerService customerService = new CustomerService();

    @FXML
    public void initialize() {
        // Load customers
        customerCombo.setItems(javafx.collections.FXCollections.observableArrayList(customerService.getAllCustomers()));

        // Define cell factory to show customer names properly
        javafx.util.Callback<ListView<com.example.loanmanagement.model.Customer>, ListCell<com.example.loanmanagement.model.Customer>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.example.loanmanagement.model.Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " (ID: " + item.getCustomerId() + ")");
            }
        };
        customerCombo.setButtonCell(cellFactory.call(null));
        customerCombo.setCellFactory(cellFactory);

        // Load Loan Types
        loanTypeCombo.setItems(javafx.collections.FXCollections
                .observableArrayList(com.example.loanmanagement.model.Loan.LoanType.values()));

        // Load Duration Options
        durationCombo.setItems(
                javafx.collections.FXCollections.observableArrayList(6, 12, 18, 24, 36, 48, 60, 120, 180, 240));

        // Add listeners for dynamic EMI calculation
        javafx.beans.value.ChangeListener<Object> listener = (obs, oldVal, newVal) -> calculateEmiPreview();
        amountField.textProperty().addListener(listener);
        interestField.textProperty().addListener(listener);
        durationCombo.valueProperty().addListener(listener);

        // Auto-fill interest rate based on loan type
        loanTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                interestField.setText(String.valueOf(newVal.getInterestRate()));
            }
        });

        // Set default start date
        startDatePicker.setValue(java.time.LocalDate.now());
    }

    private void calculateEmiPreview() {
        try {
            double principal = Double.parseDouble(amountField.getText());
            double rate = Double.parseDouble(interestField.getText()) / 12 / 100;
            Integer duration = durationCombo.getValue();

            if (principal > 0 && rate > 0 && duration != null && duration > 0) {
                double emi = (principal * rate * Math.pow(1 + rate, duration)) / (Math.pow(1 + rate, duration) - 1);
                emiPreviewLabel.setText(String.format("Rs. %,.2f", emi));
            } else {
                emiPreviewLabel.setText("0.00");
            }
        } catch (NumberFormatException | NullPointerException e) {
            emiPreviewLabel.setText("0.00");
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (customerCombo.getValue() == null || loanTypeCombo.getValue() == null
                    || durationCombo.getValue() == null) {
                showError("Please fill all required fields.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            double interest = Double.parseDouble(interestField.getText());
            int duration = durationCombo.getValue();

            com.example.loanmanagement.model.Loan loan = new com.example.loanmanagement.model.Loan();
            loan.setCustomer(customerCombo.getValue());
            loan.setType(loanTypeCombo.getValue());
            loan.setAmount(amount);
            loan.setInterestRate(interest);
            loan.setDurationMonths(duration);
            loan.setStartDate(startDatePicker.getValue());
            loan.setEndDate(startDatePicker.getValue().plusMonths(duration));

            // Service handles EMI calc and saving
            loanService.applyForLoan(loan);

            statusLabel.setText("Loan Application Submitted Successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            handleClear();

        } catch (NumberFormatException e) {
            showError("Invalid numeric input. Please check amount.");
        } catch (Exception e) {
            showError("Error submitting loan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        customerCombo.getSelectionModel().clearSelection();
        loanTypeCombo.getSelectionModel().clearSelection();
        amountField.clear();
        interestField.clear();
        durationCombo.getSelectionModel().clearSelection();
        startDatePicker.setValue(java.time.LocalDate.now());
        emiPreviewLabel.setText("0.00");
        statusLabel.setText("");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}
