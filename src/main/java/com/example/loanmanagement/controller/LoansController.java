package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.service.LoanService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class LoansController {

    @FXML
    private TilePane loanContainer;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleGroup statusGroup;

    @FXML
    private Label totalLoansLabel;
    @FXML
    private Label activeLoansLabel;
    @FXML
    private Label overdueLoansLabel;
    @FXML
    private Label totalValueLabel;

    private final LoanService loanService = new LoanService();
    private ObservableList<Loan> loanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadLoans();

        // Listen for filter changes
        statusGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterLoans();
            }
        });

        // Listen for search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterLoans());
    }

    private void loadLoans() {
        loanList.setAll(loanService.getAllLoans());
        renderLoanCards(loanList);
        calculateStats();
    }

    private void filterLoans() {
        String searchText = searchField.getText().toLowerCase();
        ToggleButton selected = (ToggleButton) statusGroup.getSelectedToggle();
        String statusFilter = selected != null ? selected.getText().toUpperCase() : "ALL";

        ObservableList<Loan> filtered = loanList.filtered(loan -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    loan.getLoanId().toLowerCase().contains(searchText) ||
                    (loan.getCustomer() != null && loan.getCustomer().getName().toLowerCase().contains(searchText));

            boolean matchesStatus = statusFilter.equals("ALL") ||
                    (loan.getStatus() != null && loan.getStatus().name().equals(statusFilter));

            return matchesSearch && matchesStatus;
        });

        renderLoanCards(filtered);
    }

    private void renderLoanCards(ObservableList<Loan> loans) {
        loanContainer.getChildren().clear();
        for (Loan loan : loans) {
            loanContainer.getChildren().add(createLoanCard(loan));
        }
    }

    private javafx.scene.Node createLoanCard(Loan loan) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMinWidth(300);

        // Header: ID + Status
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label idLabel = new Label(loan.getLoanId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        Label nameLabel = new Label(loan.getCustomer() != null ? loan.getCustomer().getName() : "Unknown");
        nameLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        titleBox.getChildren().addAll(idLabel, nameLabel);

        Region r = new Region();
        HBox.setHgrow(r, javafx.scene.layout.Priority.ALWAYS);

        Label statusBadge = new Label(loan.getStatus() != null ? loan.getStatus().name() : "PENDING");
        statusBadge.getStyleClass().add("status-badge");
        String status = loan.getStatus() != null ? loan.getStatus().name() : "PENDING";
        if (status.contains("ACTIVE"))
            statusBadge.getStyleClass().add("status-active");
        else if (status.contains("OVERDUE"))
            statusBadge.getStyleClass().add("status-overdue");
        else if (status.contains("CLOSED"))
            statusBadge.getStyleClass().add("status-success"); // Assuming green for closed

        header.getChildren().addAll(titleBox, r, statusBadge);

        // Grid Details
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Amount
        grid.add(new Label("Amount"), 0, 0);
        Label amountVal = new Label("Rs. " + String.format("%,.0f", loan.getAmount()));
        amountVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #1f2937;");
        grid.add(amountVal, 1, 0);

        // EMI
        grid.add(new Label("EMI"), 0, 1);
        Label emiVal = new Label("Rs. " + String.format("%,.0f", loan.getEmi()));
        emiVal.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;"); // Green
        grid.add(emiVal, 1, 1);

        // Type & Interest
        Label typeLabel = new Label(loan.getType() != null ? capitalize(loan.getType().name()) : "-");
        Label rateLabel = new Label(loan.getInterestRate() + "% p.a.");
        grid.add(typeLabel, 0, 2);
        grid.add(rateLabel, 1, 2);

        // Styling grid labels
        grid.getChildren().stream()
                .filter(n -> n instanceof Label && GridPane.getColumnIndex(n) == 0)
                .forEach(n -> n.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;"));

        // Progress Bar
        VBox progressBox = new VBox(5);
        int totalEmis = loan.getTotalEmis() > 0 ? loan.getTotalEmis() : loan.getDurationMonths(); // Fallback
        int paidEmis = loan.getPaidEmis();
        double progress = totalEmis > 0 ? (double) paidEmis / totalEmis : 0;

        HBox progressLabels = new HBox();
        Label pLabel = new Label("Progress");
        pLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        Region pr = new Region();
        HBox.setHgrow(pr, javafx.scene.layout.Priority.ALWAYS);
        Label countLabel = new Label(paidEmis + "/" + totalEmis + " EMIs");
        countLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        progressLabels.getChildren().addAll(pLabel, pr, countLabel);

        ProgressBar pBar = new ProgressBar(progress);
        pBar.setMaxWidth(Double.MAX_VALUE);
        pBar.setStyle("-fx-accent: #1f2937;"); // Dark progress bar

        progressBox.getChildren().addAll(progressLabels, pBar);

        // Footer Actions
        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER);
        Button viewBtn = new Button("View Details");
        viewBtn.getStyleClass().add("button-secondary");
        viewBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(viewBtn, javafx.scene.layout.Priority.ALWAYS);

        viewBtn.setOnAction(e -> openLoanDetails(loan));

        Button menuBtn = new Button("...");
        menuBtn.getStyleClass().add("button-secondary");

        footer.getChildren().addAll(viewBtn, menuBtn);

        card.getChildren().addAll(header, grid, new Separator(), progressBox, footer);
        return card;
    }

    private void openLoanDetails(Loan loan) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/example/loanmanagement/loan_details.fxml"));
            javafx.scene.Parent root = loader.load();

            LoanDetailsController controller = loader.getController();
            controller.setLoan(loan);

            if (DashboardController.getInstance() != null) {
                DashboardController.getInstance().loadViewNode(root);
            }
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    private void calculateStats() {
        if (totalLoansLabel == null)
            return;
        totalLoansLabel.setText(String.valueOf(loanList.size()));

        long active = loanList.stream().filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE).count();
        activeLoansLabel.setText(String.valueOf(active));

        long overdue = loanList.stream().filter(l -> l.getStatus() == Loan.LoanStatus.OVERDUE).count();
        overdueLoansLabel.setText(String.valueOf(overdue));

        double totalValue = loanList.stream().mapToDouble(Loan::getAmount).sum();
        totalValueLabel.setText("Rs. " + String.format("%,.0f", totalValue));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
