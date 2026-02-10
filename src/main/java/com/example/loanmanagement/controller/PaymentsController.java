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
    @FXML
    private Label statusLabel;

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
        // Filter active loans only? maybe
        List<Loan> activeLoans = loans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE || l.getStatus() == Loan.LoanStatus.PENDING) // Allow
                                                                                                                  // pending
                                                                                                                  // just
                                                                                                                  // in
                                                                                                                  // case
                .collect(Collectors.toList());
        loanCombo.setItems(javafx.collections.FXCollections.observableArrayList(activeLoans));
    }

    private void updateLoanDetails(Loan loan) {
        outstandingLabel.setText(String.format("%.2f", loan.getOutstandingAmount()));
        emiLabel.setText(String.format("%.2f", loan.getEmi()));
        amountField.setText(String.format("%.2f", loan.getEmi())); // Auto-fill EMI amount
    }

    @FXML
    private void handleSavePayment() {
        try {
            if (loanCombo.getValue() == null || amountField.getText().isEmpty() || methodCombo.getValue() == null) {
                showError("Please fill all required fields.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showError("Amount must be positive.");
                return;
            }

            Payment payment = new Payment();
            payment.setLoan(loanCombo.getValue());
            payment.setCustomerName(customerCombo.getValue().getName()); // Simplified
            payment.setAmount(amount);
            payment.setMethod(methodCombo.getValue());
            payment.setDate(paymentDatePicker.getValue());

            paymentService.recordPayment(payment);

            statusLabel.setText("Payment Recorded Successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            // Refresh loan details to show updated outstanding
            // Force reload logic or just manually update UI?
            // Ideally reload from DB.
            updateLoanDetails(loanCombo.getValue());
            // Warning: updateLoanDetails uses the OLD object from combo unless we refresh
            // it.
            // But paymentService modifies the loan in DB. The object in memory might be
            // stale.
            // We should re-fetch.

            handleClear();
            // Actually, clearing resets everything.

        } catch (NumberFormatException e) {
            showError("Invalid amount format.");
        } catch (Exception e) {
            showError("Error recording payment: " + e.getMessage());
            e.printStackTrace();
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
        statusLabel.setText("");
        paymentDatePicker.setValue(LocalDate.now());
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }
}
