package com.capstone.javabox.csit228.games.bookwormbattle;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Tile {
    public int r, c;
    public char letter;
    public boolean isLocked = false;
    public GameConstants.GemType gemType = null;
    public StackPane pane = new StackPane();
    private Label label = new Label();

    public Tile(int r, int c, char l) {
        this.r = r; this.c = c; this.letter = l;
        pane.setPrefSize(70, 70);
        label.setFont(Font.font("System", FontWeight.BOLD, 28));
        pane.getChildren().add(label);
        updateVisuals();
    }

    public void setGem(GameConstants.GemType type) {
        this.gemType = type;
        updateVisuals();
    }

    public void updateVisuals() {
        label.setText(String.valueOf(letter));

        // Default color
        String color = "white";
        label.setTextFill(Color.BLACK);

        if (gemType != null) {
            color = gemType.color; // Orange, Blue, etc.
            label.setTextFill(Color.WHITE);
        }

        // Use -fx-background-color: color !important style logic
        pane.setStyle("-fx-background-color: " + color + ";" +
                "-fx-border-color: #333333;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;");

        pane.setOpacity(isLocked ? 0.3 : 1.0);
    }

    public void setSelected(boolean s) {
        if (s) pane.setStyle(pane.getStyle() + "-fx-border-color: #f1c40f; -fx-border-width: 4;");
        else updateVisuals();
    }
}