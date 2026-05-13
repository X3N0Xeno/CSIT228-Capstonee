package com.capstone.javabox.csit228.games;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public interface JavaboxGame {
    String getGameTitle();
    void launchGame(Stage activeStage, Runnable onQuit);

    //This function should be called within your launchGame.
    /*
    * It takes the Stage and Runnable passed from the javabox lobby (it's just the parameter of launchGame)
    * The window title of the game
    * The fxml file name
    * The width of your window
    * The height of your window
    *
    */
    default void loadAndLaunch(Stage activeStage, Runnable onQuit, String title, String fxml, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(), width, height);
            JavaboxAbstractController controller = loader.getController();
            controller.setQuitCallback(onQuit);
            activeStage.setTitle(title);
            activeStage.setScene(scene);
            activeStage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Failed to load game FXML: " + fxml);
            e.printStackTrace();
        }
    }
}