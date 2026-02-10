package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.service.LoanService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class LoansController {

    @FXML
    private TableView<Loan> loansTable;
    @FXML
    private TableColumn<Loan, String> colLoanId;
    @FXML
    private TableColumn<Loan, String> colCustomer;
    @FXML
    private TableColumn<Loan, Double> colAmount;
    @FXML
    private TableColumn<Loan, String> colDate;
    @FXML
    private TableColumn<Loan, String> colStatus;
    @FXML
    private TableColumn<Loan, Void> colAction;

    private final LoanService loanService = new LoanService();
    private ObservableList<Loan> loanList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup columns
        colLoanId.setCellValueFactory(new PropertyValueFactory<>("loanId"));
        colCustomer.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCustomer() != null ? cellData.getValue().getCustomer().getName() : "Unknown"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load data
        loadLoans();
    }

    private void loadLoans() {
        loanList.setAll(loanService.getAllLoans());
        loansTable.setItems(loanList);
    }

    @FXML
    private void handleNewLoan() {
        if (DashboardController.getInstance() != null) {
            DashboardController.getInstance().loadView("loan_application.fxml");
        }
    }
}
