package com.capstone.javabox.csit228.games.wordle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class StatisticsManager {

    private static final String FILE_NAME = "wordle-stats.txt";
    private static final int MAX_GUESSES = 8;

    private int totalGamesPlayed;
    private int totalWins;
    private int currentStreak;
    private int bestStreak;
    private int[] guessDistribution;

    public StatisticsManager() {
        guessDistribution = new int[MAX_GUESSES];
        load();
    }

    public void recordGame(WordleGame game) {
        totalGamesPlayed++;
        if (game.isWin()) {
            totalWins++;
            currentStreak++;
            if (currentStreak > bestStreak) bestStreak = currentStreak;
            int index = game.getGuessesUsed() - 1;
            if (index >= 0 && index < MAX_GUESSES) guessDistribution[index]++;
        } else {
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
            for (int i = 0; i < MAX_GUESSES; i++) {
                bw.write("dist" + (i + 1) + "=" + guessDistribution[i]);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save stats: " + e.getMessage());
        }
    }

    private void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return; // First time, use defaults (all zero)

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Map<String, Integer> data = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    data.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                }
            }
            totalGamesPlayed = data.getOrDefault("totalGamesPlayed", 0);
            totalWins        = data.getOrDefault("totalWins", 0);
            currentStreak    = data.getOrDefault("currentStreak", 0);
            bestStreak       = data.getOrDefault("bestStreak", 0);
            for (int i = 0; i < MAX_GUESSES; i++) {
                guessDistribution[i] = data.getOrDefault("dist" + (i + 1), 0);
            }
        } catch (IOException e) {
            System.out.println("Could not load stats: " + e.getMessage());
        }
    }

    // Getters
    public int getTotalGamesPlayed() { return totalGamesPlayed; }
    public int getTotalWins()        { return totalWins; }
    public int getCurrentStreak()    { return currentStreak; }
    public int getBestStreak()       { return bestStreak; }
    public int[] getGuessDistribution() { return guessDistribution; }
    public double getWinRate() {
        if (totalGamesPlayed == 0) return 0;
        return (totalWins * 100.0) / totalGamesPlayed;
    }
}
