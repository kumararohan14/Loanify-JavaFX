package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.control.TableCell;

import java.time.LocalDate;

public class LoanDetailsController {

    @FXML
    private Label subtitleLabel;
    @FXML
    private Label loanIdLabel;
    @FXML
    private Label statusBadge;
    @FXML
    private Label loanTypeLabel;
    @FXML
    private Label amountLabel;
    @FXML
    private Label outstandingLabelHeader;
    @FXML
    private Label customerNameLabel;
    @FXML
    private Label startDateLabel;
    @FXML
    private Label endDateLabel;
    @FXML
    private Label rateLabel;

    @FXML
    private Label progressTextLabel;
    @FXML
    private ProgressBar emiProgressBar;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private Label remainingEmisLabel;

    @FXML
    private Label paidEmisCountLabel;
    @FXML
    private Label monthlyEmiLabel;
    @FXML
    private Label totalDurationLabel;

    @FXML
    private Label totalPaidLabel;
    @FXML
    private Label totalOutstandingLabel;
    @FXML
    private Label nextEmiDateLabel;

    @FXML
    private TableView<EmiScheduleItem> scheduleTable;
    @FXML
    private TableColumn<EmiScheduleItem, Integer> colIndex;
    @FXML
    private TableColumn<EmiScheduleItem, String> colDate;
    @FXML
    private TableColumn<EmiScheduleItem, Double> colPrincipal;
    @FXML
    private TableColumn<EmiScheduleItem, Double> colInterest;
    @FXML
    private TableColumn<EmiScheduleItem, Double> colTotal;
    @FXML
    private TableColumn<EmiScheduleItem, String> colStatus;

    private Loan loan;

    @FXML
    public void initialize() {
        setupTable();
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
        populateData();
    }

    private void setupTable() {
        colIndex.setCellValueFactory(cellData -> cellData.getValue().indexProperty().asObject());

        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colDate.setCellFactory(col -> new TableCell<EmiScheduleItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setText(null);
                else {
                    setText(item);
                    setStyle("-fx-text-fill: #6b7280;");
                }
            }
        });

        colPrincipal.setCellValueFactory(cellData -> cellData.getValue().principalProperty().asObject());
        colPrincipal.setCellFactory(col -> new MoneyCell());

        colInterest.setCellValueFactory(cellData -> cellData.getValue().interestProperty().asObject());
        colInterest.setCellFactory(col -> new MoneyCell());

        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty().asObject());
        colTotal.setCellFactory(col -> new MoneyCell());

        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<EmiScheduleItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    if ("Paid".equalsIgnoreCase(item)) {
                        badge.getStyleClass().add("status-active"); // Green like active
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #10b981; -fx-font-size: 10px;");
                    } else {
                        badge.getStyleClass().add("status-pending"); // Grey/Orange
                        badge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-font-size: 10px;");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    private void populateData() {
        if (loan == null)
            return;

        // Header & Main Info
        subtitleLabel.setText("Viewing " + loan.getLoanId());
        loanIdLabel.setText(loan.getLoanId());

        statusBadge.setText(loan.getStatus() != null ? loan.getStatus().name() : "PENDING");
        statusBadge.getStyleClass().clear();
        statusBadge.getStyleClass().add("status-badge");
        String status = statusBadge.getText();
        if (status.contains("ACTIVE"))
            statusBadge.getStyleClass().add("status-active");
        else if (status.contains("OVERDUE"))
            statusBadge.getStyleClass().add("status-overdue");
        else if (status.contains("CLOSED"))
            statusBadge.getStyleClass().add("status-success");

        loanTypeLabel.setText(loan.getType() != null ? capitalize(loan.getType().name()) + " Loan" : "Loan");
        amountLabel.setText("Rs. " + formatMoney(loan.getAmount()));

        // Calculate Outstanding (Total EMIs * EMI Amount - Paid Amount)
        // Or simpler: Remaining EMIs * EMI Amount
        int paid = loan.getPaidEmis();
        int total = loan.getTotalEmis() > 0 ? loan.getTotalEmis() : loan.getDurationMonths();
        double emi = loan.getEmi();
        double outstanding = (total - paid) * emi;
        outstandingLabelHeader.setText("Outstanding: Rs. " + formatMoney(outstanding));
        totalOutstandingLabel.setText("Rs. " + formatMoney(outstanding));

        customerNameLabel.setText(loan.getCustomer() != null ? loan.getCustomer().getName() : "-");
        startDateLabel.setText(loan.getStartDate() != null ? loan.getStartDate().toString() : "-");
        endDateLabel.setText(loan.getEndDate() != null ? loan.getEndDate().toString() : "-");
        rateLabel.setText(loan.getInterestRate() + "% p.a.");

        // Progress
        progressTextLabel.setText(paid + " of " + total + " EMIs paid");
        double progress = total > 0 ? (double) paid / total : 0;
        emiProgressBar.setProgress(progress);
        progressPercentLabel.setText(String.format("%.1f", progress * 100) + "% Complete");
        remainingEmisLabel.setText((total - paid) + " EMIs remaining");

        paidEmisCountLabel.setText(String.valueOf(paid));
        monthlyEmiLabel.setText("Rs. " + formatMoney(emi));
        totalDurationLabel.setText(String.valueOf(total));

        // Side Stats
        double totalPaid = paid * emi;
        totalPaidLabel.setText("Rs. " + formatMoney(totalPaid));

        LocalDate nextDue = loan.getStartDate() != null ? loan.getStartDate().plusMonths(paid + 1) : LocalDate.now();
        nextEmiDateLabel.setText(nextDue.toString()); // Could resize date input format

        // Generate Schedule
        generateSchedule(total, paid, emi, loan.getStartDate());
    }

    private void generateSchedule(int totalMonths, int paidMonths, double emiAmount, LocalDate start) {
        ObservableList<EmiScheduleItem> items = FXCollections.observableArrayList();
        if (start == null)
            start = LocalDate.now();

        // Simple amortization simulation (Principal/Interest split is constant for
        // simplicity unless we do complex math)
        // For accurate display, we usually need a stored schedule. Here we mock split
        // for UI demo (e.g. 70% Principal, 30% Interest)
        // In real app, this should come from DB.

        double interestPart = emiAmount * 0.2; // Mock 20% interest
        double principalPart = emiAmount - interestPart;

        for (int i = 1; i <= totalMonths; i++) {
            LocalDate date = start.plusMonths(i);
            String status = i <= paidMonths ? "Paid" : "Pending";

            items.add(new EmiScheduleItem(i, date.toString(), principalPart, interestPart, emiAmount, status));
        }
        scheduleTable.setItems(items);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount);
    }

    @FXML
    private void handleBack() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().loadView("loans.fxml");
        }
    }

    // Helper Class for Table
    public static class EmiScheduleItem {
        private final javafx.beans.property.IntegerProperty index;
        private final javafx.beans.property.StringProperty date;
        private final javafx.beans.property.DoubleProperty principal;
        private final javafx.beans.property.DoubleProperty interest;
        private final javafx.beans.property.DoubleProperty total;
        private final javafx.beans.property.StringProperty status;

        public EmiScheduleItem(int index, String date, Double principal, Double interest, Double total, String status) {
            this.index = new javafx.beans.property.SimpleIntegerProperty(index);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.principal = new javafx.beans.property.SimpleDoubleProperty(principal);
            this.interest = new javafx.beans.property.SimpleDoubleProperty(interest);
            this.total = new javafx.beans.property.SimpleDoubleProperty(total);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.IntegerProperty indexProperty() {
            return index;
        }

        public javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        public javafx.beans.property.DoubleProperty principalProperty() {
            return principal;
        }

        public javafx.beans.property.DoubleProperty interestProperty() {
            return interest;
        }

        public javafx.beans.property.DoubleProperty totalProperty() {
            return total;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    // Helper Cell for Money
    private class MoneyCell extends TableCell<EmiScheduleItem, Double> {
        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText("Rs. " + formatMoney(item));
            }
        }
    }
}
