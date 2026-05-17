package com.capstone.javabox.csit228.games.fruitsnake;

import com.capstone.javabox.csit228.games.JavaboxGame;
import javafx.stage.Stage;

public class FruitSnakeLauncher implements JavaboxGame {

    @Override
    public String getGameTitle() {
        return "Fruit Snake";
    }

    @Override
    public void launchGame(Stage activeStage, Runnable onQuit) {
        loadAndLaunch(activeStage, onQuit, "Fruit Snake",
                "fruit-snake-view.fxml", 700, 780);
    }
}