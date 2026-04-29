package com.health.healthsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            HelloApplication.class.getResource("auth-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 480, 420);
        stage.setTitle("Healthcare Management System — Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
