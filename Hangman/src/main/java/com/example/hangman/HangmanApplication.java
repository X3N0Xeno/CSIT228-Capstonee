package com.example.hangman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HangmanApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/hangman/hangman-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 620, 700);
        stage.setTitle("Hangman x Russian Roulette");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}