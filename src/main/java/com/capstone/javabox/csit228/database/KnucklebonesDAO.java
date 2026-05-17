package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class KnucklebonesDAO {

    public static void saveMatch(String player1Name, int score1, String player2Name, int score2) {
        // Determine who won (1 for win, 0 for loss/tie)
        int p1Win = (score1 > score2) ? 1 : 0;
        int p2Win = (score2 > score1) ? 1 : 0;

        // Process both players independently
        updatePlayerStats(player1Name, score1, p1Win);
        updatePlayerStats(player2Name, score2, p2Win);
    }

    private static void updatePlayerStats(String username, int score, int win) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT highest_score FROM Knucklebones_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO Knucklebones_Stats (player_id, games_played, wins, highest_score) VALUES (?, 1, ?, ?)";
        // GREATEST() is a lifesaver here—it just picks the larger of the two numbers!
        String updateQuery = "UPDATE Knucklebones_Stats SET games_played = games_played + 1, wins = wins + ?, highest_score = GREATEST(highest_score, ?) WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Player exists, update their row
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, win);
                        updateStmt.setInt(2, score);
                        updateStmt.setInt(3, playerId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // First time playing Knucklebones, insert new row
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.setInt(2, win);
                        insertStmt.setInt(3, score);
                        insertStmt.executeUpdate();
                    }
                }
            }
            System.out.println("Knucklebones stats successfully saved for " + username + "!");

        } catch (Exception e) {
            System.err.println("Database error while saving Knucklebones score for: " + username);
            e.printStackTrace();
        }
    }
}