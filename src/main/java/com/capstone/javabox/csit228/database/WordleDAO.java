package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WordleDAO {

    public static void saveGame(String username, boolean won, int guessesUsed) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save Wordle score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT current_streak, best_streak FROM Wordle_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO Wordle_Stats (player_id) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int currentStreak = 0;
            int bestStreak = 0;

            // 1. Check if they exist and get their current streaks
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    currentStreak = rs.getInt("current_streak");
                    bestStreak = rs.getInt("best_streak");
                } else {
                    // Create base row if they don't exist
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.executeUpdate();
                    }
                }
            }

            // 2. Calculate the new values based on the win/loss
            int newStreak = won ? currentStreak + 1 : 0;
            int newBestStreak = Math.max(bestStreak, newStreak);
            int winIncrement = won ? 1 : 0;

            // 3. Dynamically build the update query to hit the correct 'dist_X' column
            StringBuilder updateQuery = new StringBuilder(
                    "UPDATE Wordle_Stats SET games_played = games_played + 1, wins = wins + ?, current_streak = ?, best_streak = ?"
            );

            if (won && guessesUsed >= 1 && guessesUsed <= 8) {
                updateQuery.append(", dist_").append(guessesUsed).append(" = dist_").append(guessesUsed).append(" + 1");
            }
            updateQuery.append(" WHERE player_id = ?");

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery.toString())) {
                updateStmt.setInt(1, winIncrement);
                updateStmt.setInt(2, newStreak);
                updateStmt.setInt(3, newBestStreak);
                updateStmt.setInt(4, playerId);
                updateStmt.executeUpdate();
            }

            System.out.println("Wordle++ stats successfully uploaded to leaderboard for " + username + "!");

        } catch (Exception e) {
            System.err.println("Database error while saving Wordle++ score.");
            e.printStackTrace();
        }
    }
}