package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.model.Payment;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import com.example.loanmanagement.service.PaymentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentsController {

    @FXML
    private ComboBox<Customer> customerCombo;
    @FXML
    private ComboBox<Loan> loanCombo;
    @FXML
    private Label outstandingLabel;
    @FXML
    private Label emiLabel;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<Payment.PaymentMethod> methodCombo;
    @FXML
    private DatePicker paymentDatePicker;

    private final CustomerService customerService = new CustomerService();
    private final LoanService loanService = new LoanService();
    private final PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        // Load customers
        customerCombo.setItems(javafx.collections.FXCollections.observableArrayList(customerService.getAllCustomers()));

        // Customer Combo Cell Factory
        Callback<ListView<Customer>, ListCell<Customer>> customerCellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " (" + item.getCustomerId() + ")");
            }
        };
        customerCombo.setButtonCell(customerCellFactory.call(null));
        customerCombo.setCellFactory(customerCellFactory);

        // Loan Combo Cell Factory
        Callback<ListView<Loan>, ListCell<Loan>> loanCellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Loan item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : "Loan: " + item.getLoanId() + " - " + item.getStatus());
            }
        };
        loanCombo.setButtonCell(loanCellFactory.call(null));
        loanCombo.setCellFactory(loanCellFactory);

        // Load Methods
        methodCombo.setItems(javafx.collections.FXCollections.observableArrayList(Payment.PaymentMethod.values()));

        // Default Date
        paymentDatePicker.setValue(LocalDate.now());

        // Listeners
        customerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadLoansForCustomer(newVal);
            } else {
                loanCombo.getItems().clear();
            }
        });

        loanCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateLoanDetails(newVal);
            } else {
                outstandingLabel.setText("-");
                emiLabel.setText("-");
                amountField.clear();
            }
        });
    }

    private void loadLoansForCustomer(Customer customer) {
        List<Loan> loans = loanService.getLoansByCustomer(customer.getId());
        List<Loan> activeLoans = loans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE || l.getStatus() == Loan.LoanStatus.PENDING)
                .collect(Collectors.toList());
        loanCombo.setItems(javafx.collections.FXCollections.observableArrayList(activeLoans));
    }

    private void updateLoanDetails(Loan loan) {
        outstandingLabel.setText(String.format("%.2f", loan.getOutstandingAmount()));
        emiLabel.setText(String.format("%.2f", loan.getEmi()));
        amountField.setText(String.format("%.2f", loan.getEmi()));
    }

    @FXML
    private void handleSavePayment() {
        if (loanCombo.getValue() == null || customerCombo.getValue() == null
                || amountField.getText().isEmpty() || methodCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid amount format.");
            return;
        }

        Payment payment = new Payment();
        payment.setLoan(loanCombo.getValue());
        payment.setCustomerName(customerCombo.getValue().getName());
        payment.setAmount(amount);
        payment.setMethod(methodCombo.getValue());
        payment.setDate(paymentDatePicker.getValue());

        try {
            boolean success = paymentService.recordPayment(payment);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Payment recorded successfully!");

                // Refresh loan details after payment
                Loan updatedLoan = loanService.getLoanById(loanCombo.getValue().getId());
                loanCombo.getItems().remove(loanCombo.getValue());
                loanCombo.getItems().add(updatedLoan);
                loanCombo.setValue(updatedLoan);
                updateLoanDetails(updatedLoan);

                // Clear only amount and method
                amountField.clear();
                methodCombo.getSelectionModel().clearSelection();
            } else {
                showAlert(Alert.AlertType.ERROR, "Payment Failed", "Payment could not be recorded. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error recording payment: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        customerCombo.getSelectionModel().clearSelection();
        loanCombo.getSelectionModel().clearSelection();
        loanCombo.getItems().clear();
        amountField.clear();
        methodCombo.getSelectionModel().clearSelection();
        outstandingLabel.setText("-");
        emiLabel.setText("-");
        paymentDatePicker.setValue(LocalDate.now());
    }

    // Helper method to show pop-up alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
