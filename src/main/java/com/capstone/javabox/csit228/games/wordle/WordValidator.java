package com.capstone.javabox.csit228.games.wordle;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class WordValidator implements Runnable {

    private static final String FILE_NAME = "src/main/java/com/capstone/javabox/csit228/games/5-letter-words.txt";
    private final Set<String> wordSet = new HashSet<>();
    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordSet.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            System.out.println("Could not load word list: " + e.getMessage());
        } finally {
            latch.countDown(); // Signal that loading is done
        }
    }

    // Blocks until the word list is fully loaded
    public boolean isValid(String word) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return wordSet.contains(word.toLowerCase().trim());
    }

    public boolean isReady() {
        return latch.getCount() == 0;
    }
}