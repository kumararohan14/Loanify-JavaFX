package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

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
    private ComboBox<Integer> durationCombo;
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

        customerCombo.setItems(
                javafx.collections.FXCollections.observableArrayList(customerService.getAllCustomers()));

        javafx.util.Callback<ListView<Customer>, ListCell<Customer>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? ""
                        : item.getName() + " (ID: " + item.getCustomerId() + ")");
            }
        };
        customerCombo.setButtonCell(cellFactory.call(null));
        customerCombo.setCellFactory(cellFactory);

        loanTypeCombo.setItems(
                javafx.collections.FXCollections.observableArrayList(Loan.LoanType.values()));

        durationCombo.setItems(
                javafx.collections.FXCollections.observableArrayList(
                        6, 12, 18, 24, 36, 48, 60, 120, 180, 240));

        javafx.beans.value.ChangeListener<Object> listener =
                (obs, oldVal, newVal) -> calculateEmiPreview();

        amountField.textProperty().addListener(listener);
        interestField.textProperty().addListener(listener);
        durationCombo.valueProperty().addListener(listener);

        loanTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                interestField.setText(String.valueOf(newVal.getInterestRate()));
            }
        });

        startDatePicker.setValue(LocalDate.now());

        // ❗ Disable past dates in DatePicker UI
        startDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                }
            }
        });
    }

    private void calculateEmiPreview() {
        try {
            double principal = Double.parseDouble(amountField.getText());
            double rate = Double.parseDouble(interestField.getText()) / 12 / 100;
            Integer duration = durationCombo.getValue();

            if (principal > 0 && rate > 0 && duration != null && duration > 0) {
                double emi = (principal * rate * Math.pow(1 + rate, duration))
                        / (Math.pow(1 + rate, duration) - 1);
                emiPreviewLabel.setText(String.format("Rs. %,.2f", emi));
            } else {
                emiPreviewLabel.setText("0.00");
            }
        } catch (Exception e) {
            emiPreviewLabel.setText("0.00");
        }
    }

    @FXML
    private void handleSubmit() {
        try {

            if (customerCombo.getValue() == null
                    || loanTypeCombo.getValue() == null
                    || durationCombo.getValue() == null
                    || amountField.getText().isEmpty()
                    || interestField.getText().isEmpty()
                    || startDatePicker.getValue() == null) {

                showError("Please fill all required fields.");
                return;
            }

            Customer selectedCustomer = customerCombo.getValue();

            // ✅ Only ACTIVE customers allowed
            if (!"ACTIVE".equalsIgnoreCase(selectedCustomer.getStatus().toString())) {
                showError("Only ACTIVE customers can apply for loans.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());

            // ✅ Amount validation
            if (amount <= 0) {
                showError("Loan amount must be greater than 0.");
                return;
            }

            if (amount < 10000 || amount > 10000000) {
                showError("Loan amount must be between Rs 10,000 and Rs 10,000,000.");
                return;
            }

            double interest = Double.parseDouble(interestField.getText());
            int duration = durationCombo.getValue();
            LocalDate startDate = startDatePicker.getValue();

            // ✅ Start date validation
            if (startDate.isBefore(LocalDate.now())) {
                showError("Start date cannot be a past date.");
                return;
            }

            Loan loan = new Loan();
            loan.setCustomer(selectedCustomer);
            loan.setType(loanTypeCombo.getValue());
            loan.setAmount(amount);
            loan.setInterestRate(interest);
            loan.setDurationMonths(duration);
            loan.setStartDate(startDate);
            loan.setEndDate(startDate.plusMonths(duration));

            loanService.applyForLoan(loan);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Success");
            successAlert.setHeaderText("Loan Application Submitted");
            successAlert.setContentText("The loan application was submitted successfully!");
            successAlert.showAndWait();

            statusLabel.setText("Loan Application Submitted Successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");

            handleClear();

        } catch (NumberFormatException e) {
            showError("Invalid numeric input. Please check amount and interest.");
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
        startDatePicker.setValue(LocalDate.now());
        emiPreviewLabel.setText("0.00");
        statusLabel.setText("");
    }

    private void showError(String message) {

        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");

        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText("Submission Failed");
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}
