package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UTTTDAO {

    public static void saveMatch(String p1Name, String p2Name, char winnerSymbol) {
        // Player 1 is X, Player 2 is O
        updatePlayerStats(p1Name, winnerSymbol == 'X', winnerSymbol == 'D');
        updatePlayerStats(p2Name, winnerSymbol == 'O', winnerSymbol == 'D');
        System.out.println("UTTT Match successfully uploaded for " + p1Name + " and " + p2Name + "!");
    }

    private static void updatePlayerStats(String username, boolean isWin, boolean isDraw) {
        int playerId = PlayerDAO.getOrAddPlayerId(username);

        if (playerId == -1) {
            System.err.println("Could not save UTTT score. Player ID generation failed for: " + username);
            return;
        }

        String checkQuery = "SELECT games_played FROM UTTT_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO UTTT_Stats (player_id, games_played, wins, draws) VALUES (?, 1, ?, ?)";
        String updateQuery = "UPDATE UTTT_Stats SET games_played = games_played + 1, wins = wins + ?, draws = draws + ? WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            int winIncrement = isWin ? 1 : 0;
            int drawIncrement = isDraw ? 1 : 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, playerId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Update existing player
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, winIncrement);
                        updateStmt.setInt(2, drawIncrement);
                        updateStmt.setInt(3, playerId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // Insert new player record
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, playerId);
                        insertStmt.setInt(2, winIncrement);
                        insertStmt.setInt(3, drawIncrement);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Database error while saving UTTT score for: " + username);
            e.printStackTrace();
        }
    }
}