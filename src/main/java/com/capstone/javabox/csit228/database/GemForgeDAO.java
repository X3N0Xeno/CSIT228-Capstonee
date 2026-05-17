package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class GemForgeDAO {

    public static void saveMatch(List<String> playerNames, String winnerName, boolean isDraw) {
        String checkQuery = "SELECT games_played FROM GemForge_Stats WHERE player_id = ?";
        String insertQuery = "INSERT INTO GemForge_Stats (player_id, games_played, wins, draws) VALUES (?, 1, ?, ?)";
        String updateQuery = "UPDATE GemForge_Stats SET games_played = games_played + 1, wins = wins + ?, draws = draws + ? WHERE player_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            for (String playerName : playerNames) {
                // Get or create the player in the core Players table
                int playerId = PlayerDAO.getOrAddPlayerId(playerName);
                if (playerId == -1) {
                    System.err.println("Skipping save for " + playerName + " due to ID generation failure.");
                    continue;
                }

                // Determine if THIS specific player won or drew
                int winIncrement = (!isDraw && playerName.equals(winnerName)) ? 1 : 0;
                int drawIncrement = isDraw ? 1 : 0;

                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, playerId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        // Player exists in Gem Forge, update their stats
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, winIncrement);
                            updateStmt.setInt(2, drawIncrement);
                            updateStmt.setInt(3, playerId);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // First time playing Gem Forge, insert new row
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                            insertStmt.setInt(1, playerId);
                            insertStmt.setInt(2, winIncrement);
                            insertStmt.setInt(3, drawIncrement);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Gem Forge match successfully uploaded to the database!");

        } catch (Exception e) {
            System.err.println("Database error while saving Gem Forge match.");
            e.printStackTrace();
        }
    }
}