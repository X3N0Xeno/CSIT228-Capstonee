module com.capstone.javabox.csit228 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.sql;

    //MAIN APP STUFF
    exports com.capstone.javabox.csit228;
    opens com.capstone.javabox.csit228 to javafx.fxml;

    //GAMES FOLDER
    exports com.capstone.javabox.csit228.games to javafx.graphics;
    opens com.capstone.javabox.csit228.games to javafx.fxml;

    exports com.capstone.javabox.csit228.games.hangman;
    opens com.capstone.javabox.csit228.games.hangman to javafx.fxml;

    exports com.capstone.javabox.csit228.games.fullhouse;
    opens com.capstone.javabox.csit228.games.fullhouse to javafx.fxml;

    exports com.capstone.javabox.csit228.games.knucklebones;
    opens com.capstone.javabox.csit228.games.knucklebones to javafx.fxml;

    exports com.capstone.javabox.csit228.games.windowswarm to javafx.graphics;
    opens com.capstone.javabox.csit228.games.windowswarm to javafx.fxml;

    exports com.capstone.javabox.csit228.games.previews to javafx.graphics;
    opens com.capstone.javabox.csit228.games.previews to javafx.fxml;

    exports com.capstone.javabox.csit228.games.wordle;
    opens com.capstone.javabox.csit228.games.wordle to javafx.fxml;

    exports com.capstone.javabox.csit228.games.ultimatettt;
    opens com.capstone.javabox.csit228.games.ultimatettt to javafx.fxml;

    opens com.capstone.javabox.csit228.games.bookwormbattle to javafx.fxml;
    exports com.capstone.javabox.csit228.games.bookwormbattle;

    exports com.capstone.javabox.csit228.games.fruitsnake;
    opens com.capstone.javabox.csit228.games.fruitsnake to javafx.fxml;

    exports com.capstone.javabox.csit228.games.chainreaction;
    opens com.capstone.javabox.csit228.games.chainreaction to javafx.fxml;

    exports com.capstone.javabox.csit228.games.gemforge;
    opens com.capstone.javabox.csit228.games.gemforge to javafx.fxml;

    exports com.capstone.javabox.csit228.database;
    opens com.capstone.javabox.csit228.database to javafx.fxml;

    exports com.capstone.javabox.csit228.games.leaderboard;
    opens com.capstone.javabox.csit228.games.leaderboard to javafx.fxml;
}