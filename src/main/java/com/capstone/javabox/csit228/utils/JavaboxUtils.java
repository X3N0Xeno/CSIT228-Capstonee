package com.capstone.javabox.csit228.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

//This is a utility class, perfect for global helper function calls!
public final class JavaboxUtils {
    private JavaboxUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    //Call showAlert to create a popup.
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //Call showConfirmation to create a yes or no popup. This also returns a boolean; The answer of the user.
    public static boolean showConfirmation(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    //This is a helper function that extracts hex [for colors] if you
    public static String extractHex(String style) {
        if (style == null || !style.contains("#")) return "white";
        return style.substring(style.indexOf("#"), style.indexOf("#") + 7);
    }
}
