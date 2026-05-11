package com.capstone.javabox.csit228.games.windowswarm;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SwarmWindowController {
    @FXML
    private Label spamLabel;
    public void setSpamText(String text) {
        spamLabel.setText(text);
    }
}
