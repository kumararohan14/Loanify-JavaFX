package com.example.loanmanagement.util;

import com.example.loanmanagement.LoanApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static Stage primaryStage;
    private static final Map<String, Parent> viewCache = new HashMap<>();

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(LoanApplication.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 800);
            scene.getStylesheets().add(LoanApplication.class.getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Parent loadView(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoanApplication.class.getResource(fxml));
        return loader.load();
    }
}
