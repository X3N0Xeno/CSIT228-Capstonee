package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class WindowSwarmDAO {

    public static void saveScore(String username, int finalScore) {
        // 1. Get or create the player using the DAO we made earlier!
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save score. Player ID generation failed.");
            return;
        }

        String checkQuery = "SELECT highest_score, total_runs FROM WindowSwarm_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO WindowSwarm_Stats (player_id, highest_score, total_runs) VALUES (?, ?, 1)";
        String updateQuery = "UPDATE WindowSwarm_Stats SET highest_score = ?, total_runs = total_runs + 1 WHERE player_id = ?";
        String updateRunsOnlyQuery = "UPDATE WindowSwarm_Stats SET total_runs = total_runs + 1 WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Check if the player already has a record for Window Swarm
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Player exists in Window Swarm! Let's check their high score.
                    int currentHighScore = rs.getInt("highest_score");

                    if (finalScore > currentHighScore) {
                        // New High Score! Update score and increment runs
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, finalScore);
                            updateStmt.setInt(2, playerId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // Didn't beat high score, just increment total runs
                        try (PreparedStatement runStmt = conn.prepareStatement(updateRunsOnlyQuery)) {
                            runStmt.setInt(1, playerId);
                            runStmt.executeUpdate();
                        }
                    }
                } else {
                    // First time playing Window Swarm! Insert a brand new row.
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.setInt(2, finalScore);
                        insertStmt.executeUpdate();
                    }
                }
            }
            System.out.println("Window Swarm stats successfully saved for " + username + "!");

        } catch (Exception e) {
            System.err.println("Database error while saving Window Swarm score.");
            e.printStackTrace();
        }
    }
}