module com.example.hangman {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hangman to javafx.fxml;
    exports com.example.hangman;
}