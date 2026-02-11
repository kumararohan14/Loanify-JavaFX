module com.example.loanmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;
    requires java.naming;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;
    requires twilio;

    opens com.example.loanmanagement to javafx.fxml;
    opens com.example.loanmanagement.controller to javafx.fxml;
    opens com.example.loanmanagement.model to org.hibernate.orm.core, javafx.base;

    exports com.example.loanmanagement;
}
