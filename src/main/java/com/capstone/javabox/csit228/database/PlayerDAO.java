package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class PlayerDAO {

    public static int getOrAddPlayerId(String username) {
        String selectQuery = "SELECT player_id FROM Players WHERE username = ?";
        String insertQuery = "INSERT INTO Players (username) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // 1. Try to find the existing player
            try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                selectStmt.setString(1, username);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("player_id"); // Returning player found
                }
            }

            // 2. If not found, create a new player
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, username);
                insertStmt.executeUpdate();

                // Fetch the newly auto-generated player_id
                ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // New player created
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching or creating player: " + username);
            e.printStackTrace();
        }

        return -1; // Return -1 if something went wrong
    }
}