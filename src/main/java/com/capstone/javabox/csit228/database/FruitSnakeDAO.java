package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FruitSnakeDAO {

    public static void saveScore(String username, int timeSurvived, int roundsCleared) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save Fruit Snake score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT games_played FROM FruitSnake_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO FruitSnake_Stats (player_id, games_played, highest_time, highest_rounds) VALUES (?, 1, ?, ?)";
        String updateQuery = "UPDATE FruitSnake_Stats SET games_played = games_played + 1, highest_time = GREATEST(highest_time, ?), highest_rounds = GREATEST(highest_rounds, ?) WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Update existing
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, timeSurvived);
                        updateStmt.setInt(2, roundsCleared);
                        updateStmt.setInt(3, playerId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.setInt(2, timeSurvived);
                        insertStmt.setInt(3, roundsCleared);
                        insertStmt.executeUpdate();
                    }
                }
            }
            System.out.println("Fruit Snake score successfully uploaded for " + username + "!");

        } catch (Exception e) {
            System.err.println("Database error while saving Fruit Snake score.");
            e.printStackTrace();
        }
    }
}