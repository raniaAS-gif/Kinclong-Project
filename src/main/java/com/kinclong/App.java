package com.kinclong;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/kinclong/Login.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Kinclong - Aplikasi Manajemen Laundry");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}