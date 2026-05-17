package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HangmanDAO {

    public static void saveGame(String username, boolean won, int pullsSurvived) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save Hangman score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT current_streak, best_streak FROM Hangman_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO Hangman_Stats (player_id) VALUES (?)";

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

            // 2. Calculate the new values
            int newStreak = won ? currentStreak + 1 : 0;
            int newBestStreak = Math.max(bestStreak, newStreak);
            int winIncrement = won ? 1 : 0;
            int deathIncrement = won ? 0 : 1;

            // 3. Update the global stats
            String updateQuery = "UPDATE Hangman_Stats SET games_played = games_played + 1, " +
                    "wins = wins + ?, current_streak = ?, best_streak = ?, " +
                    "total_pulls_survived = total_pulls_survived + ?, " +
                    "total_deaths = total_deaths + ? WHERE player_id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, winIncrement);
                updateStmt.setInt(2, newStreak);
                updateStmt.setInt(3, newBestStreak);
                updateStmt.setInt(4, pullsSurvived);
                updateStmt.setInt(5, deathIncrement);
                updateStmt.setInt(6, playerId);
                updateStmt.executeUpdate();
            }

            System.out.println("Hangman stats successfully uploaded to leaderboard for " + username + "!");

        } catch (Exception e) {
            System.err.println("Database error while saving Hangman score.");
            e.printStackTrace();
        }
    }
}