package com.capstone.javabox.csit228.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardDAO {

    // A simple record class to hold the data
    public static class Entry {
        public final String username;
        public final int score;
        public Entry(String username, int score) {
            this.username = username;
            this.score = score;
        }
    }

    private static List<Entry> fetchTop(String query) {
        List<Entry> topList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                topList.add(new Entry(rs.getString("username"), rs.getInt("val")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topList;
    }

    public static List<Entry> getKnucklebonesTop() {
        return fetchTop("SELECT p.username, k.highest_score as val FROM Knucklebones_Stats k JOIN Players p ON k.player_id = p.player_id ORDER BY k.highest_score DESC LIMIT 10");
    }

    public static List<Entry> getWindowSwarmTop() {
        return fetchTop("SELECT p.username, w.highest_score as val FROM WindowSwarm_Stats w JOIN Players p ON w.player_id = p.player_id ORDER BY w.highest_score DESC LIMIT 10");
    }

    public static List<Entry> getWordleTop() {
        return fetchTop("SELECT p.username, w.wins as val FROM Wordle_Stats w JOIN Players p ON w.player_id = p.player_id ORDER BY w.wins DESC LIMIT 10");
    }

    public static List<Entry> getHangmanTop() {
        return fetchTop("SELECT p.username, h.wins as val FROM Hangman_Stats h JOIN Players p ON h.player_id = p.player_id ORDER BY h.wins DESC LIMIT 10");
    }

    public static List<Entry> getUTTTTop() {
        return fetchTop("SELECT p.username, u.wins as val FROM UTTT_Stats u JOIN Players p ON u.player_id = p.player_id ORDER BY u.wins DESC LIMIT 10");
    }

    public static List<Entry> getFullHouseTop() {
        return fetchTop("SELECT p.username, f.bets_won as val FROM FullHouse_Player_Stats f JOIN Players p ON f.player_id = p.player_id ORDER BY f.bets_won DESC LIMIT 10");
    }
}