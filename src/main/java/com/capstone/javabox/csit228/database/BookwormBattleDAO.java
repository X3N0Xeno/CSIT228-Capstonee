package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BookwormBattleDAO {

    public static void saveMatch(String p1Name, String p2Name, String winnerName) {
        updatePlayerStats(p1Name, p1Name.equals(winnerName));
        updatePlayerStats(p2Name, p2Name.equals(winnerName));
        System.out.println("Bookworm Battle match successfully uploaded!");
    }

    private static void updatePlayerStats(String username, boolean isWin) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT games_played FROM BookwormBattle_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO BookwormBattle_Stats (player_id, games_played, wins) VALUES (?, 1, ?)";
        String updateQuery = "UPDATE BookwormBattle_Stats SET games_played = games_played + 1, wins = wins + ? WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int winIncrement = isWin ? 1 : 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, winIncrement);
                        updateStmt.setInt(2, playerId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.setInt(2, winIncrement);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error while saving Bookworm Battle score for: " + username);
            e.printStackTrace();
        }
    }
}