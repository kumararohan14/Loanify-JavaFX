package com.example.loanmanagement.controller;

import com.example.loanmanagement.exception.SmsSendingException;
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

        colAction.setCellFactory(col -> new TableCell<Customer, Void>() {

    private final Button btnEdit = new Button();
    private final Button btnDelete = new Button();
    private final HBox pane = new HBox(8);

    {
        pane.setAlignment(javafx.geometry.Pos.CENTER);

        // ===== EDIT BUTTON =====
        javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
        editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1.003 1.003 0 000-1.42l-2.34-2.34a1.003 1.003 0 00-1.42 0l-1.83 1.83 3.75 3.75 1.84-1.82z");
        editIcon.setFill(javafx.scene.paint.Color.web("#3b82f6"));
        btnEdit.setGraphic(editIcon);
        btnEdit.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
        btnEdit.setTooltip(new Tooltip("Edit Customer"));
        btnEdit.setOnAction(event -> {
            Customer customer = getTableRow().getItem();
            if (customer != null) handleEditCustomer(customer);
        });

        // ===== DELETE BUTTON =====
        javafx.scene.shape.SVGPath trashIcon = new javafx.scene.shape.SVGPath();
        trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashIcon.setFill(javafx.scene.paint.Color.web("#ef4444"));
        btnDelete.setGraphic(trashIcon);
        btnDelete.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
        btnDelete.setTooltip(new Tooltip("Delete Customer"));
        btnDelete.setOnAction(event -> {
            Customer customer = getTableRow().getItem();
            if (customer != null) handleDeleteCustomer(customer);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
            setGraphic(null);
        } else {
            Customer customer = getTableRow().getItem();
            pane.getChildren().clear();

            // Always add Edit + Delete
            pane.getChildren().addAll(btnEdit, btnDelete);

            // Dynamically add Resend OTP **only if status is PENDING**
            if (customer.getStatus() == Customer.Status.PENDING) {
                Button btnResendOtp = new Button("Resend OTP");
                btnResendOtp.setStyle("-fx-background-color: #facc15; -fx-cursor: hand;");
                btnResendOtp.setTooltip(new Tooltip("Resend OTP"));
                btnResendOtp.setOnAction(e -> {
                    try {
                        customerService.resendOtp(customer.getNic());
                    } catch (SmsSendingException e1) {
                        // SMS failed
            Alert smsAlert = new Alert(Alert.AlertType.WARNING);
            smsAlert.setTitle("OTP Not Sent");
            smsAlert.setContentText(
                    "Failed to send OTP to: " + customer.getPhone() +
                    "\nReason: " + e1.getMessage() +
                    "\nClick 'Retry' to resend OTP."
            );

            ButtonType retryBtn = new ButtonType("Retry");
            smsAlert.getButtonTypes().setAll(retryBtn, ButtonType.CANCEL);

            Optional<ButtonType> response = smsAlert.showAndWait();
            if (response.isPresent() && response.get() == retryBtn) {
                try {
                    customerService.resendOtp(customer.getNic());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("OTP Resent");
                    alert.setHeaderText(null);
                    alert.setContentText("A new OTP has been sent to " + customer.getPhone());
                    alert.showAndWait();
                } catch (SmsSendingException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Failed to resend OTP: " + ex.getMessage());
                    alert.showAndWait();
                }
            }

            // Allow OTP verification dialog even if first SMS failed
            showOtpVerificationDialog(customer);
            loadCustomers();
                    }
                });
                pane.getChildren().add(btnResendOtp);
            }

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


    // Utility method to show validation alert
private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}


    @FXML
private void handleAddCustomer() {
    Dialog<Customer> dialog = new Dialog<>();
    dialog.setTitle("Add New Customer");
    dialog.setHeaderText("Enter Customer Details");

    ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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

    // --- VALIDATION: prevent dialog from closing on invalid input ---
    Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
    saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
        String nameValue = name.getText().trim();
        String nicValue = nic.getText().trim();
        String phoneValue = phone.getText().trim();
        String emailValue = email.getText().trim();
        String addressValue = address.getText().trim();

        if (nameValue.isEmpty()) {
            showAlert("Validation Error", "Name cannot be empty.");
            event.consume();
            return;
        }
        if (!nicValue.matches("^\\d{9}[VvXx]$|^\\d{12}$")) {
            showAlert("Validation Error", "Invalid NIC. Must be 9 digits + V/X or 12 digits.");
            event.consume();
            return;
        }
        if (!phoneValue.matches("^(?:\\+94|0)(7[0-9]{8})$")) {
            showAlert("Validation Error", "Invalid phone number. Must be Sri Lankan format (0xxxxxxx or +947xxxxxxx).");
            event.consume();
            return;
        }
        if (!emailValue.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            showAlert("Validation Error", "Invalid email address.");
            event.consume();
            return;
        }
        if (addressValue.isEmpty()) {
            showAlert("Validation Error", "Address cannot be empty.");
            event.consume();
        }
    });

    // --- Result converter ---
    dialog.setResultConverter(dialogButton -> {
        if (dialogButton == saveButtonType) {
            Customer c = new Customer();
            c.setName(name.getText().trim());
            c.setNic(nic.getText().trim());
            c.setPhone(phone.getText().trim());
            c.setEmail(email.getText().trim());
            c.setAddress(address.getText().trim());
            c.setCustomerId("CUST-" + System.currentTimeMillis() % 10000);
            return c;
        }
        return null;
    });

    Optional<Customer> result = dialog.showAndWait();

    result.ifPresent(customer -> {
        try {
            customerService.addCustomer(customer);
            // OTP sent successfully
            showOtpVerificationDialog(customer);
            loadCustomers();
        } catch (SmsSendingException e) {
            // Customer saved, but SMS failed
            Alert smsAlert = new Alert(Alert.AlertType.WARNING);
            smsAlert.setTitle("OTP Not Sent");
            smsAlert.setHeaderText("Customer saved but OTP could not be sent");
            smsAlert.setContentText(
                    "Failed to send OTP to: " + customer.getPhone() +
                    "\nReason: " + e.getMessage() +
                    "\nClick 'Retry' to resend OTP."
            );

            ButtonType retryBtn = new ButtonType("Retry");
            smsAlert.getButtonTypes().setAll(retryBtn, ButtonType.CANCEL);

            Optional<ButtonType> response = smsAlert.showAndWait();
            if (response.isPresent() && response.get() == retryBtn) {
                try {
                    customerService.resendOtp(customer.getNic());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("OTP Resent");
                    alert.setHeaderText(null);
                    alert.setContentText("A new OTP has been sent to " + customer.getPhone());
                    alert.showAndWait();
                } catch (SmsSendingException e1) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Failed to resend OTP: " + e1.getMessage());
                    alert.showAndWait();
                }
            }

            // Allow OTP verification dialog even if first SMS failed
            showOtpVerificationDialog(customer);
            loadCustomers();
        } catch (Exception e) {
            showAlert("Error", "Could not save customer: " + e.getMessage());
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

    // --- Edit Customer ---
private void handleEditCustomer(Customer customer) {
    Dialog<Customer> dialog = new Dialog<>();
    dialog.setTitle("Edit Customer");
    dialog.setHeaderText("Update Customer Details");

    ButtonType saveButton = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new javafx.geometry.Insets(20));

    TextField name = new TextField(customer.getName());
    TextField nic = new TextField(customer.getNic());
    TextField phone = new TextField(customer.getPhone());
    TextField email = new TextField(customer.getEmail());
    TextField address = new TextField(customer.getAddress());

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

    // --- Validation ---
    Button updateBtn = (Button) dialog.getDialogPane().lookupButton(saveButton);
    updateBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
        if (name.getText().trim().isEmpty()) { showAlert("Validation Error", "Name cannot be empty."); event.consume(); return; }
        if (!nic.getText().trim().matches("^\\d{9}[VvXx]$|^\\d{12}$")) { showAlert("Validation Error", "Invalid NIC."); event.consume(); return; }
        if (!phone.getText().trim().matches("^(?:\\+94|0)(7[0-9]{8})$")) { showAlert("Validation Error", "Invalid phone."); event.consume(); return; }
        if (!email.getText().trim().matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) { showAlert("Validation Error", "Invalid email."); event.consume(); return; }
        if (address.getText().trim().isEmpty()) { showAlert("Validation Error", "Address cannot be empty."); event.consume(); }
    });

    dialog.setResultConverter(button -> {
        if (button == saveButton) {
            customer.setName(name.getText().trim());
            customer.setNic(nic.getText().trim());
            customer.setPhone(phone.getText().trim());
            customer.setEmail(email.getText().trim());
            customer.setAddress(address.getText().trim());
            return customer;
        }
        return null;
    });

    Optional<Customer> result = dialog.showAndWait();
    result.ifPresent(updated -> {
        customerService.updateCustomer(updated);
        loadCustomers();
    });
}


}
