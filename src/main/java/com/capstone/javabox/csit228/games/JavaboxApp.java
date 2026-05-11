package com.capstone.javabox.csit228.games;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JavaboxApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JavaboxApp.class.getResource("javabox-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Welcome to Javabox");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}