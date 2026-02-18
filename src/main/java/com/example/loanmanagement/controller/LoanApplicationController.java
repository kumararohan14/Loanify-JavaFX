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
    private Spinner<Integer> durationSpinner; // Changed to Spinner
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
        javafx.util.Callback<ListView<Customer>, ListCell<Customer>> cellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (ID: " + item.getCustomerId() + ")");
            }
        };
        customerCombo.setButtonCell(cellFactory.call(null));
        customerCombo.setCellFactory(cellFactory);

        // Load Loan Types
        loanTypeCombo.setItems(javafx.collections.FXCollections
                .observableArrayList(Loan.LoanType.values()));

        // Initialize Spinner with default value factory
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 12);
        durationSpinner.setValueFactory(valueFactory);

        // Add listeners for dynamic EMI calculation and Rate Update
        javafx.beans.value.ChangeListener<Object> listener = (obs, oldVal, newVal) -> {
            updateInterestRate();
            calculateEmiPreview();
        };
        amountField.textProperty().addListener(listener);
        interestField.textProperty().addListener((obs, oldVal, newVal) -> calculateEmiPreview()); // Interest change
                                                                                                  // only needs to
                                                                                                  // recalc EMI
        durationSpinner.valueProperty().addListener(listener);

        // Auto-fill interest rate and duration config based on loan type
        loanTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                configureDurationSpinner(newVal);
                // Interest update is handled inside configure -> updateInterestRate
            }
        });

        // Set default start date
        startDatePicker.setValue(java.time.LocalDate.now());

        // Disable past dates
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

    private void configureDurationSpinner(Loan.LoanType type) {
        SpinnerValueFactory<Integer> factory;
        if (type == Loan.LoanType.DAY) {
            // Default 65, Step 1
            factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 65, 1);
        } else if (type == Loan.LoanType.WEEK) {
            // Default 13, Step 1
            factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 300, 13, 1);
        } else {
            // Default
            factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 360, 12, 1);
        }
        durationSpinner.setValueFactory(factory);

        // Trigger initial rate calculation
        updateInterestRate();
    }

    // NEW METHOD: Update Interest Rate based on Duration
    private void updateInterestRate() {
        Loan.LoanType type = loanTypeCombo.getValue();
        Integer duration = durationSpinner.getValue();

        if (type != null && duration != null) {
            if (type == Loan.LoanType.DAY) {
                // Base: 30% for 65 days
                // Effective Rate = 30 * (Duration / 65.0)
                double effectiveRate = 30.0 * (duration / 65.0);
                interestField.setText(String.format("%.2f", effectiveRate));
            } else if (type == Loan.LoanType.WEEK) {
                // Base: 30% for 13 weeks
                // Effective Rate = 30 * (Duration / 13.0)
                double effectiveRate = 30.0 * (duration / 13.0);
                interestField.setText(String.format("%.2f", effectiveRate));
            } else {
                // Keep existing or read from Enum?
                // Legacy types have fixed rates in Enum.
                interestField.setText(String.valueOf(type.getInterestRate()));
            }
        }
    }

    private void calculateEmiPreview() {
        try {
            double principal = Double.parseDouble(amountField.getText());
            Loan.LoanType type = loanTypeCombo.getValue();
            Integer duration = durationSpinner.getValue();

            if (principal > 0 && duration != null && duration > 0 && type != null) {
                double installmentAmount;

                if (type == Loan.LoanType.DAY || type == Loan.LoanType.WEEK) {
                    // Use the CALCULATED Rate from the text field
                    // This rate is the effective rate for the WHOLE term.
                    // Interest = Principal * (Rate / 100)
                    double effectiveRate = Double.parseDouble(interestField.getText());
                    double totalInterest = principal * (effectiveRate / 100.0);

                    // Add Document Charges (Rs. 650)
                    double documentCharges = 650.0;

                    double totalPayable = principal + totalInterest + documentCharges;
                    installmentAmount = totalPayable / duration;

                } else {
                    // Legacy Fallback (Standard EMI)
                    double rate = Double.parseDouble(interestField.getText()) / 12 / 100;
                    installmentAmount = (principal * rate * Math.pow(1 + rate, duration))
                            / (Math.pow(1 + rate, duration) - 1);
                }

                emiPreviewLabel.setText(String.format("Rs. %,.2f", installmentAmount));
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
                    || durationSpinner.getValue() == null
                    || amountField.getText().isEmpty()
                    || interestField.getText().isEmpty()
                    || startDatePicker.getValue() == null) {
                showError("Please fill all required fields.");
                return;
            }

            Customer selectedCustomer = customerCombo.getValue();
            if (!"ACTIVE".equalsIgnoreCase(selectedCustomer.getStatus().toString())) {
                showError("Only ACTIVE customers can apply for loans.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showError("Loan amount must be greater than 0.");
                return;
            }

            double interest = Double.parseDouble(interestField.getText());
            int duration = durationSpinner.getValue();
            LocalDate startDate = startDatePicker.getValue();
            if (startDate.isBefore(LocalDate.now())) {
                showError("Start date cannot be a past date.");
                return;
            }

            Loan.LoanType type = loanTypeCombo.getValue();

            Loan loan = new Loan();
            loan.setCustomer(selectedCustomer);
            loan.setType(type);
            loan.setAmount(amount);
            loan.setInterestRate(interest);
            loan.setDurationMonths(duration); // Reusing field for Days/Weeks
            loan.setStartDate(startDate);
            loan.setDocumentCharges(650.0); // Fixed Document Charges

            // End Date Logic
            if (type == Loan.LoanType.DAY) {
                loan.setEndDate(startDate.plusDays(duration));
            } else if (type == Loan.LoanType.WEEK) {
                loan.setEndDate(startDate.plusWeeks(duration));
            } else {
                loan.setEndDate(startDate.plusMonths(duration));
            }

            // Service handles EMI calc and saving
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
        // Reset Spinner
        if (durationSpinner.getValueFactory() != null) {
            durationSpinner.getValueFactory().setValue(12); // Default
            if (loanTypeCombo.getValue() == Loan.LoanType.DAY)
                durationSpinner.getValueFactory().setValue(65);
            if (loanTypeCombo.getValue() == Loan.LoanType.WEEK)
                durationSpinner.getValueFactory().setValue(13);
        }
        startDatePicker.setValue(java.time.LocalDate.now());
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
