package com.capstone.javabox.csit228.games.hangman;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HangmanStatisticsManager {

    private static final String FILE_NAME = "hangman-stats.txt";

    private int totalGamesPlayed;
    private int totalWins;
    private int currentStreak;
    private int bestStreak;
    private int totalPullsSurvived;
    private int totalDeaths;

    public HangmanStatisticsManager() {
        load();
    }

    public void recordGame(HangmanGame game) {
        totalGamesPlayed++;
        totalPullsSurvived += game.getPullsSurvived();

        if (game.isWin()) {
            totalWins++;
            currentStreak++;
            if (currentStreak > bestStreak) bestStreak = currentStreak;
        } else {
            totalDeaths++;
            currentStreak = 0;
        }

        save();
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            bw.write("totalGamesPlayed=" + totalGamesPlayed); bw.newLine();
            bw.write("totalWins=" + totalWins);               bw.newLine();
            bw.write("currentStreak=" + currentStreak);       bw.newLine();
            bw.write("bestStreak=" + bestStreak);             bw.newLine();
            bw.write("totalPullsSurvived=" + totalPullsSurvived); bw.newLine();
            bw.write("totalDeaths=" + totalDeaths);           bw.newLine();
        } catch (IOException e) {
            System.out.println("Could not save stats: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Map<String, Integer> data = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    data.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
            totalGamesPlayed    = data.getOrDefault("totalGamesPlayed", 0);
            totalWins           = data.getOrDefault("totalWins", 0);
            currentStreak       = data.getOrDefault("currentStreak", 0);
            bestStreak          = data.getOrDefault("bestStreak", 0);
            totalPullsSurvived  = data.getOrDefault("totalPullsSurvived", 0);
            totalDeaths         = data.getOrDefault("totalDeaths", 0);
        } catch (IOException e) {
            System.out.println("Could not load stats: " + e.getMessage());
        }
    }

    public int getTotalGamesPlayed()   { return totalGamesPlayed; }
    public int getTotalWins()          { return totalWins; }
    public int getCurrentStreak()      { return currentStreak; }
    public int getBestStreak()         { return bestStreak; }
    public int getTotalPullsSurvived() { return totalPullsSurvived; }
    public int getTotalDeaths()        { return totalDeaths; }
    public double getWinRate() {
        if (totalGamesPlayed == 0) return 0;
        return (totalWins * 100.0) / totalGamesPlayed;
    }
}