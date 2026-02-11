module com.example.loanmanagement {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.loanmanagement to javafx.fxml;
    exports com.example.loanmanagement;
}
