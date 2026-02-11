package com.example.loanmanagement;

import javafx.application.Application;

import javafx.stage.Stage;
import com.example.loanmanagement.util.SceneManager;

import java.io.IOException;

public class LoanApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}
