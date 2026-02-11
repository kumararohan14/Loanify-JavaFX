package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.model.Payment;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import com.example.loanmanagement.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea reportTextArea;

    private final LoanService loanService = new LoanService();
    private final CustomerService customerService = new CustomerService();
    private final PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Loan Portfolio Summary",
                "Payment History",
                "Overdue Loans Report",
                "Customer List"));

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerateReport() {
        String type = reportTypeCombo.getValue();
        if (type == null) {
            showAlert("Please select a report type.");
            return;
        }

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start != null && end != null && start.isAfter(end)) {
            showAlert("Start date cannot be after end date.");
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("========================================\n");
        report.append("   LOAN MANAGEMENT SYSTEM - REPORT   \n");
        report.append("========================================\n");
        report.append("Report Type: ").append(type).append("\n");
        report.append("Generated On: ").append(LocalDate.now()).append("\n");
        if (start != null && end != null) {
            report.append("Period: ").append(start).append(" to ").append(end).append("\n");
        }
        report.append("========================================\n\n");

        switch (type) {
            case "Loan Portfolio Summary":
                generateLoanSummary(report);
                break;
            case "Payment History":
                generatePaymentHistory(report, start, end);
                break;
            case "Overdue Loans Report":
                generateOverdueReport(report);
                break;
            case "Customer List":
                generateCustomerList(report);
                break;
        }

        reportTextArea.setText(report.toString());
    }

    private void generateLoanSummary(StringBuilder report) {
        List<Loan> loans = loanService.getAllLoans();
        double totalDisbursed = loans.stream().mapToDouble(Loan::getAmount).sum();
        double totalOutstanding = loans.stream().mapToDouble(Loan::getOutstandingAmount).sum();
        long activeCount = loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE).count();
        long closedCount = loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.CLOSED).count();

        report.append(String.format("Total Loans Disbursed: %d\n", loans.size()));
        report.append(String.format("Total Amount Disbursed: Rs. %.2f\n", totalDisbursed));
        report.append(String.format("Total Outstanding Amount: Rs. %.2f\n", totalOutstanding));
        report.append("----------------------------------------\n");
        report.append(String.format("Active Loans: %d\n", activeCount));
        report.append(String.format("Closed Loans: %d\n", closedCount));
        report.append(String.format("Overdue Loans: %d\n",
                loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.OVERDUE).count()));
        report.append(String.format("Pending Applications: %d\n",
                loans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.PENDING).count()));
    }

    private void generatePaymentHistory(StringBuilder report, LocalDate start, LocalDate end) {
        List<Payment> payments = paymentService.getAllPayments();

        // Filter by date if provided
        if (start != null && end != null) {
            payments = payments.stream()
                    .filter(p -> !p.getDate().isBefore(start) && !p.getDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        report.append(String.format("%-15s %-20s %-15s %-15s\n", "Date", "Customer", "Amount", "Method"));
        report.append("----------------------------------------------------------------\n");

        double total = 0;
        for (Payment p : payments) {
            report.append(String.format("%-15s %-20s Rs. %-11.2f %-15s\n",
                    p.getDate(),
                    p.getCustomerName(),
                    p.getAmount(),
                    p.getMethod()));
            total += p.getAmount();
        }
        report.append("----------------------------------------------------------------\n");
        report.append(String.format("Total Collections: Rs. %.2f\n", total));
    }

    private void generateOverdueReport(StringBuilder report) {
        List<Loan> overdueLoans = loanService.getAllLoans().stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.OVERDUE)
                .collect(Collectors.toList());

        if (overdueLoans.isEmpty()) {
            report.append("No overdue loans found.\n");
            return;
        }

        report.append(String.format("%-10s %-20s %-15s %-15s\n", "Loan ID", "Customer", "Outstanding", "Start Date"));
        report.append("----------------------------------------------------------------\n");

        for (Loan l : overdueLoans) {
            report.append(String.format("%-10s %-20s Rs. %-11.2f %-15s\n",
                    l.getLoanId(),
                    l.getCustomer() != null ? l.getCustomer().getName() : "Unknown",
                    l.getOutstandingAmount(),
                    l.getStartDate()));
        }
    }

    private void generateCustomerList(StringBuilder report) {
        List<Customer> customers = customerService.getAllCustomers();

        report.append(String.format("%-20s %-15s %-15s %-30s\n", "Name", "NIC", "Phone", "Email"));
        report.append("--------------------------------------------------------------------------------\n");

        for (Customer c : customers) {
            report.append(String.format("%-20s %-15s %-15s %-30s\n",
                    c.getName(),
                    c.getNic(),
                    c.getPhone(),
                    c.getEmail()));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
