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
    private TextField durationField;
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

        // Add listeners for dynamic EMI calculation
        javafx.beans.value.ChangeListener<String> listener = (obs, oldVal, newVal) -> calculateEmiPreview();
        amountField.textProperty().addListener(listener);
        interestField.textProperty().addListener(listener);
        durationField.textProperty().addListener(listener);

        // Set default start date
        startDatePicker.setValue(java.time.LocalDate.now());
    }

    private void calculateEmiPreview() {
        try {
            double principal = Double.parseDouble(amountField.getText());
            double rate = Double.parseDouble(interestField.getText()) / 12 / 100;
            int time = Integer.parseInt(durationField.getText());

            if (principal > 0 && rate > 0 && time > 0) {
                double emi = (principal * rate * Math.pow(1 + rate, time)) / (Math.pow(1 + rate, time) - 1);
                emiPreviewLabel.setText(String.format("%.2f", emi));
            }
        } catch (NumberFormatException e) {
            emiPreviewLabel.setText("0.00");
        }
    }

    @FXML
    private void handleSubmit() {
        try {
            if (customerCombo.getValue() == null || loanTypeCombo.getValue() == null) {
                showError("Please select a customer and loan type.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            double interest = Double.parseDouble(interestField.getText());
            int duration = Integer.parseInt(durationField.getText());

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
            showError("Invalid numeric input. Please check amount, interest, and duration.");
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
        durationField.clear();
        startDatePicker.setValue(java.time.LocalDate.now());
        emiPreviewLabel.setText("0.00");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}
