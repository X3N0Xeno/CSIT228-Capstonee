package com.capstone.javabox.csit228.database;

import com.capstone.javabox.csit228.games.fullhouse.House;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class FullHouseDAO {

    public static void saveRaceResults(House winningHouse, List<House> activeHouses) {
        String houseQuery = "INSERT INTO FullHouse_House_Stats (house_name, races_run, races_won) VALUES (?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE races_run = races_run + 1, races_won = races_won + ?";

        String playerQuery = "INSERT INTO FullHouse_Player_Stats (player_id, bets_placed, bets_won) VALUES (?, 1, ?) " +
                "ON DUPLICATE KEY UPDATE bets_placed = bets_placed + 1, bets_won = bets_won + ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            for (House h : activeHouses) {
                boolean isWinner = (h.name.equals(winningHouse.name));
                int houseWinIncrement = isWinner ? 1 : 0;

                // 1. Update the House's global stats
                try (PreparedStatement houseStmt = conn.prepareStatement(houseQuery)) {
                    houseStmt.setString(1, h.name);
                    houseStmt.setInt(2, houseWinIncrement);
                    houseStmt.setInt(3, houseWinIncrement);
                    houseStmt.executeUpdate();
                }

                // 2. Update all the players who bet on this house
                for (String bettorName : h.getBettors()) {
                    int playerId = PlayerDAO.getOrAddPlayerId(bettorName);
                    if (playerId != -1) {
                        try (PreparedStatement playerStmt = conn.prepareStatement(playerQuery)) {
                            playerStmt.setInt(1, playerId);
                            playerStmt.setInt(2, houseWinIncrement);
                            playerStmt.setInt(3, houseWinIncrement);
                            playerStmt.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Full House race results successfully committed to the database!");

        } catch (Exception e) {
            System.err.println("Database error while saving Full House results.");
            e.printStackTrace();
        }
    }

    // A helper method to fetch a house's actual win record to display on their selection card
    public static String getHouseGlobalRecord(String houseName) {
        String query = "SELECT races_run, races_won FROM FullHouse_House_Stats WHERE house_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, houseName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int runs = rs.getInt("races_run");
                int wins = rs.getInt("races_won");
                int winRate = (runs > 0) ? (int) (((double) wins / runs) * 100) : 0;
                return "Global Wins: " + wins + " (" + winRate + "%)";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Global Wins: 0 (0%)"; // Fallback if they haven't raced yet
    }
}