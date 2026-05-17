package com.capstone.javabox.csit228.games.fruitsnake;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FruitSnakeScoreManager {

    private static final String FILE_NAME = "fruit-snake-score.txt";

    private int highScore;
    private int highRounds;

    public FruitSnakeScoreManager() {
        load();
    }

    public void recordScore(int score, int rounds) {
        boolean updated = false;
        if (score > highScore)   { highScore = score;   updated = true; }
        if (rounds > highRounds) { highRounds = rounds; updated = true; }
        if (updated) save();
    }

    private void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
            bw.write("highScore=" + highScore);   bw.newLine();
            bw.write("highRounds=" + highRounds); bw.newLine();
        } catch (IOException e) {
            System.out.println("Could not save score: " + e.getMessage());
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
                if (parts.length == 2)
                    data.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            }
            highScore  = data.getOrDefault("highScore", 0);
            highRounds = data.getOrDefault("highRounds", 0);
        } catch (IOException e) {
            System.out.println("Could not load score: " + e.getMessage());
        }
    }

    public int getHighScore()  { return highScore; }
    public int getHighRounds() { return highRounds; }
}