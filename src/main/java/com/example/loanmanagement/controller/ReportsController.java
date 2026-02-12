package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.model.Payment;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import com.example.loanmanagement.service.PaymentService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
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
    @FXML
    private Button btnExportPdf;
    @FXML
    private Button btnExportExcel;

    private final LoanService loanService = new LoanService();
    private final CustomerService customerService = new CustomerService();
    private final PaymentService paymentService = new PaymentService();

    private String currentReportText = "";

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
        report.append("LOAN MANAGEMENT SYSTEM REPORT\n");
        report.append("Report Type: ").append(type).append("\n");
        report.append("Generated On: ").append(LocalDate.now()).append("\n\n");

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

        currentReportText = report.toString();
        reportTextArea.setText(currentReportText);

        btnExportPdf.setDisable(false);
        btnExportExcel.setDisable(false);
    }

    // ================= PDF EXPORT =================

    @FXML
    private void handleExportPdf() {
        if (currentReportText.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(reportTextArea.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();
            document.add(new Paragraph(currentReportText));
            document.close();
            showAlert("PDF Exported Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= EXCEL EXPORT =================

    @FXML
    private void handleExportExcel() {
        if (currentReportText.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showSaveDialog(reportTextArea.getScene().getWindow());
        if (file == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Report");

            String[] lines = currentReportText.split("\n");

            for (int i = 0; i < lines.length; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue(lines[i]);
            }

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();

            showAlert("Excel Exported Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REPORT METHODS =================

    private void generateLoanSummary(StringBuilder report) {
        List<Loan> loans = loanService.getAllLoans();
        double totalDisbursed = loans.stream().mapToDouble(Loan::getAmount).sum();
        double totalOutstanding = loans.stream().mapToDouble(Loan::getOutstandingAmount).sum();

        report.append("Total Loans: ").append(loans.size()).append("\n");
        report.append("Total Disbursed: Rs. ").append(totalDisbursed).append("\n");
        report.append("Total Outstanding: Rs. ").append(totalOutstanding).append("\n");
    }

    private void generatePaymentHistory(StringBuilder report, LocalDate start, LocalDate end) {
        List<Payment> payments = paymentService.getAllPayments();

        if (start != null && end != null) {
            payments = payments.stream()
                    .filter(p -> !p.getDate().isBefore(start) && !p.getDate().isAfter(end))
                    .collect(Collectors.toList());
        }

        double total = 0;
        for (Payment p : payments) {
            report.append(p.getDate()).append(" - ")
                    .append(p.getCustomerName()).append(" - Rs. ")
                    .append(p.getAmount()).append("\n");
            total += p.getAmount();
        }

        report.append("\nTotal Collections: Rs. ").append(total);
    }

    private void generateOverdueReport(StringBuilder report) {
        List<Loan> overdueLoans = loanService.getAllLoans().stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.OVERDUE)
                .collect(Collectors.toList());

        for (Loan l : overdueLoans) {
            report.append(l.getLoanId()).append(" - ")
                    .append(l.getCustomer().getName()).append(" - Rs. ")
                    .append(l.getOutstandingAmount()).append("\n");
        }
    }

    private void generateCustomerList(StringBuilder report) {
        List<Customer> customers = customerService.getAllCustomers();
        for (Customer c : customers) {
            report.append(c.getName()).append(" - ")
                    .append(c.getPhone()).append(" - ")
                    .append(c.getEmail()).append("\n");
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
