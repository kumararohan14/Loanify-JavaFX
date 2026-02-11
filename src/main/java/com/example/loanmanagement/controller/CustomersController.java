package com.example.loanmanagement.controller;

import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML
    private Label totalCustomersLabel;
    @FXML
    private Label activeCustomersLabel; // Using 'Active' status or similar logic
    @FXML
    private Label withActiveLoansLabel;
    @FXML
    private Label inactiveCustomersLabel;

    private final CustomerService customerService = new CustomerService();
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadCustomers();
    }

    private void setupTableColumns() {
        // Customer Column: Avatar + Name + ID
        colName.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Customer customer = getTableRow().getItem();
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                    // Avatar Placeholder
                    Label avatar = new Label(getInitials(customer.getName()));
                    avatar.setStyle(
                            "-fx-background-color: #e0e7ff; -fx-text-fill: #3730a3; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: CENTER; -fx-font-weight: bold;");

                    VBox text = new VBox(2);
                    Label name = new Label(customer.getName());
                    name.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-primary;");
                    Label id = new Label(customer.getCustomerId());
                    id.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 11px;");
                    text.getChildren().addAll(name, id);

                    box.getChildren().addAll(avatar, text);
                    setGraphic(box);
                }
            }
        });

        // Contact Column: Phone + Email
        colContact.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Customer c = getTableRow().getItem();
                    VBox box = new VBox(2);

                    Label phone = new Label(c.getPhone() != null ? "\u260E " + c.getPhone() : "-");
                    phone.setStyle("-fx-text-fill: -color-text-secondary;");

                    Label email = new Label(c.getEmail() != null ? "\u2709 " + c.getEmail() : "-");
                    email.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 11px;");

                    box.getChildren().addAll(phone, email);
                    setGraphic(box);
                }
            }
        });

        // NIC Column
        colNic.setCellValueFactory(new PropertyValueFactory<>("nic"));
        colNic.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold;");
                }
            }
        });

        // Active Loans
        colActiveLoans.setCellValueFactory(new PropertyValueFactory<>("activeLoans"));
        colActiveLoans.setCellFactory(col -> new TableCell<Customer, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " / " + "3 total"); // Mock total, or fetch real total if available
                    setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold;");
                }
            }
        });

        // Status Column
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().name() : ""));
        colStatus.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Default to Active if null/empty for now, or use item
                    String status = (item == null || item.isEmpty()) ? "Active" : item;
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");
                    if ("Active".equalsIgnoreCase(status)) {
                        badge.getStyleClass().add("status-active");
                    } else {
                        badge.getStyleClass().add("status-inactive");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Actions Column
        colAction.setCellFactory(col -> new TableCell<Customer, Void>() {
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(5);
            
            {
                // SVG Path for Trash Can (Material Design)
                javafx.scene.shape.SVGPath trashIcon = new javafx.scene.shape.SVGPath();
                trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                trashIcon.setFill(javafx.scene.paint.Color.web("#ef4444")); // Red color
                
                btnDelete.setGraphic(trashIcon);
                // Style: Transparent background, padding for click area
                btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10;");
                
                // Hover effect
                btnDelete.setOnMouseEntered(e -> {
                    btnDelete.setStyle("-fx-background-color: #fee2e2; -fx-cursor: hand; -fx-padding: 5 10 5 10; -fx-background-radius: 5;");
                });
                btnDelete.setOnMouseExited(e -> {
                    btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5 10 5 10;");
                });

                btnDelete.setTooltip(new Tooltip("Delete Customer"));

                pane.setAlignment(javafx.geometry.Pos.CENTER);
                pane.getChildren().add(btnDelete);
                
                btnDelete.setOnAction(event -> {
                    Customer customer = getTableRow().getItem();
                    if (customer != null) {
                        handleDeleteCustomer(customer);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void loadCustomers() {
        customerList.setAll(customerService.getAllCustomers());
        customersTable.setItems(customerList);
        calculateStats();
    }

    private void calculateStats() {
        int total = customerList.size();
        long active = customerList.stream().count(); // Assuming all are active for now unless we add status field
        long withLoans = customerList.stream().filter(c -> c.getActiveLoans() > 0).count();
        int inactive = 0; // Mock

        if (totalCustomersLabel != null)
            totalCustomersLabel.setText(String.valueOf(total));
        if (activeCustomersLabel != null)
            activeCustomersLabel.setText(String.valueOf(active));
        if (withActiveLoansLabel != null)
            withActiveLoansLabel.setText(String.valueOf(withLoans));
        if (inactiveCustomersLabel != null)
            inactiveCustomersLabel.setText(String.valueOf(inactive));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
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
                // Prompt for OTP
                showOtpVerificationDialog(customer);
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

    private void showOtpVerificationDialog(Customer customer) {
        com.example.loanmanagement.util.OtpDialog otpDialog = new com.example.loanmanagement.util.OtpDialog(customer.getPhone());
        
        otpDialog.setOnResend(() -> {
            try {
                customerService.resendOtp(customer.getNic());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("OTP Resent");
                alert.setHeaderText(null);
                alert.setContentText("A new OTP has been sent to " + customer.getPhone());
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to resend OTP: " + e.getMessage());
                alert.showAndWait();
            }
        });

        Optional<String> otpResult = otpDialog.showAndWait();
        if (otpResult.isPresent()) {
            try {
                boolean verified = customerService.verifyOtp(customer.getNic(), otpResult.get());
                if (verified) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Customer verified successfully!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Verification Failed");
                    alert.setHeaderText("Invalid OTP");
                    alert.setContentText("The OTP you entered is incorrect. Please try again.");
                    alert.showAndWait();
                    // Recursively show dialog again on failure? 
                    // For now, let's just show the dialog again if they failed, giving them a chance to retry.
                    showOtpVerificationDialog(customer);
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Verification Pending");
            alert.setContentText("Customer saved but not verified. Status is PENDING.");
            alert.showAndWait();
        }
    }
    private void handleDeleteCustomer(Customer customer) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete " + customer.getName() + "?");
        alert.setContentText("Are you sure you want to delete this customer? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(customer.getId());
                loadCustomers(); // Refresh table
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Success");
                success.setContentText("Customer deleted successfully.");
                success.showAndWait();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("Could not delete customer");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }
}
