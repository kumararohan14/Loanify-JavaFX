package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.model.Payment;
import com.example.loanmanagement.service.CustomerService;
import com.example.loanmanagement.service.LoanService;
import com.example.loanmanagement.service.PaymentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeDashboardController {

    @FXML
    private Label totalLoansLabel;
    @FXML
    private Label activeLoansLabel;
    @FXML
    private Label overdueLoansLabel;
    @FXML
    private Label collectionLabel;
    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label pendingAppsLabel;

    @FXML
    private LineChart<String, Number> collectionChart;
    @FXML
    private PieChart loanTypeChart;

    @FXML
    private TableView<Loan> recentLoansTable;
    @FXML
    private TableColumn<Loan, String> colLoanId;
    @FXML
    private TableColumn<Loan, String> colCustomer;
    @FXML
    private TableColumn<Loan, Double> colAmount;
    @FXML
    private TableColumn<Loan, String> colStatus;

    private final LoanService loanService = new LoanService();
    private final CustomerService customerService = new CustomerService();
    private final PaymentService paymentService = new PaymentService();

    @FXML
    public void initialize() {
        loadKpiData();
        loadCharts();
        loadRecentLoans();
    }

    private void loadKpiData() {
        List<Loan> allLoans = loanService.getAllLoans();
        long totalLoans = allLoans.size();
        long activeLoans = allLoans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE).count();
        long overdueLoans = allLoans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.OVERDUE).count();
        long pendingLoans = allLoans.stream().filter(l -> l.getStatus() == Loan.LoanStatus.PENDING).count();

        totalLoansLabel.setText(String.valueOf(totalLoans));
        activeLoansLabel.setText(String.valueOf(activeLoans));
        overdueLoansLabel.setText(String.valueOf(overdueLoans));
        pendingAppsLabel.setText(String.valueOf(pendingLoans));

        totalCustomersLabel.setText(String.valueOf(customerService.getAllCustomers().size()));

        // Calculate total collection
        List<Payment> allPayments = paymentService.getAllPayments();
        double totalCollection = allPayments.stream().mapToDouble(Payment::getAmount).sum();
        collectionLabel.setText(String.format("Rs. %.1f", totalCollection));
    }

    private void loadCharts() {
        // Pie Chart
        List<Loan> allLoans = loanService.getAllLoans();
        Map<Loan.LoanType, Long> typeCount = allLoans.stream()
                .collect(Collectors.groupingBy(Loan::getType, Collectors.counting()));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) -> pieData.add(new PieChart.Data(type.toString(), count)));
        loanTypeChart.setData(pieData);

        // Line Chart (Dummy data for now, or simple grouping by Date)
        // Grouping by Month requires Java Time logic
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Collection");
        // For now, let's just plot last 5 payments or dummy monthly if empty
        if (paymentService.getAllPayments().isEmpty()) {
            series.getData().add(new XYChart.Data<>("Jan", 10000));
            series.getData().add(new XYChart.Data<>("Feb", 15000));
            series.getData().add(new XYChart.Data<>("Mar", 12000));
        } else {
            // Real logic: Group payments by month
            // Ensuring non-empty to avoid crash
            // Simplified: Just one point "Current" -> Total
            series.getData().add(
                    new XYChart.Data<>("Current", Double.parseDouble(collectionLabel.getText().replace("Rs. ", ""))));
        }
        collectionChart.getData().add(series);
    }

    private void loadRecentLoans() {
        List<Loan> allLoans = loanService.getAllLoans();
        // Sort by ID descending (mock recent) or just take last 5
        // Assuming ID is auto-increment or list is insertion order
        List<Loan> recent = allLoans.stream().skip(Math.max(0, allLoans.size() - 5)).collect(Collectors.toList());

        ObservableList<Loan> loans = FXCollections.observableArrayList(recent);

        colLoanId.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        colCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getName() : "Unknown"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        recentLoansTable.setItems(loans);
    }

    @FXML
    private void handleNewLoan() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().loadView("loan_application.fxml");
        }
    }

    @FXML
    private void handleAddCustomer() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().loadView("customers.fxml");
        }
    }
}
