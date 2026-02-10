package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.util.Optional;

public class CustomersController {

    @FXML
    private TableView<Customer> customersTable;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colNic;
    @FXML
    private TableColumn<Customer, String> colContact;
    @FXML
    private TableColumn<Customer, Integer> colActiveLoans;
    @FXML
    private TableColumn<Customer, String> colStatus;
    @FXML
    private TableColumn<Customer, Void> colAction;

    private final CustomerService customerService = new CustomerService();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colNic.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNic() + "\n" + cellData.getValue().getCustomerId()));

        colContact.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                (cellData.getValue().getPhone() != null ? cellData.getValue().getPhone() : "-") + "\n" +
                        (cellData.getValue().getEmail() != null ? cellData.getValue().getEmail() : "-")));

        colActiveLoans.setCellValueFactory(new PropertyValueFactory<>("activeLoans"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load data
        loadCustomers();
    }

    private void loadCustomers() {
        customerList.setAll(customerService.getAllCustomers());
        customersTable.setItems(customerList);
    }

    @FXML
    private void handleAddCustomer() {
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle("Add New Customer");
        dialog.setHeaderText("Enter Customer Details");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Full Name");
        TextField nic = new TextField();
        nic.setPromptText("NIC / ID Number");
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");
        TextField email = new TextField();
        email.setPromptText("Email Address");
        TextField address = new TextField();
        address.setPromptText("Address");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("NIC:"), 0, 1);
        grid.add(nic, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phone, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(email, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(address, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a username-password-pair when the login button is
        // clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Customer c = new Customer();
                c.setName(name.getText());
                c.setNic(nic.getText());
                c.setPhone(phone.getText());
                c.setEmail(email.getText());
                c.setAddress(address.getText());
                // Generate ID
                c.setCustomerId("CUST-" + System.currentTimeMillis() % 10000);
                return c;
            }
            return null;
        });

        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {
            try {
                customerService.addCustomer(customer);
                loadCustomers(); // Refresh table
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save customer");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
    }
}
