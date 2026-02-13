package com.example.loanmanagement.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;

public class OtpDialog extends Dialog<String> {

    private final TextField otpField;
    private final ButtonType verifyButtonType;
    private final Button resendButton;

    public OtpDialog(String phoneNumber) {
        setTitle("Verify Phone Number");
        setHeaderText(null); // No default header, using custom VBox

        // Custom Layout
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: white;");

        // Icon or Title
        Label titleLabel = new Label("Enter OTP Code");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #333;");

        // Message
        Label messageLabel = new Label("We have sent a verification code to\n" + phoneNumber);
        messageLabel.setStyle("-fx-text-fill: #666; -fx-text-alignment: center;");
        messageLabel.setWrapText(true);

        // OTP Input
        otpField = new TextField();
        otpField.setPromptText("0 0 0 0 0 0");
        otpField.setAlignment(Pos.CENTER);
        otpField.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        otpField.setPrefWidth(200);
        otpField.setStyle("-fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");

        // Resend Button (Link style)
        resendButton = new Button("Resend Code");
        resendButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #007bff; -fx-cursor: hand; -fx-underline: true;");
        
        content.getChildren().addAll(titleLabel, messageLabel, otpField, resendButton);
        getDialogPane().setContent(content);

        // Buttons
        verifyButtonType = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        // Enable/Disable Verify button
        javafx.scene.Node verifyButton = getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);

        // Validation logic
        otpField.textProperty().addListener((observable, oldValue, newValue) -> {
            verifyButton.setDisable(newValue.trim().length() != 6);
        });
        
        // Focus request
        javafx.application.Platform.runLater(otpField::requestFocus);

        // Result Converter
        setResultConverter(dialogButton -> {
            if (dialogButton == verifyButtonType) {
                return otpField.getText();
            }
            return null;
        });
    }

    public void setOnResend(Runnable action) {
        resendButton.setOnAction(e -> action.run());
    }
}
