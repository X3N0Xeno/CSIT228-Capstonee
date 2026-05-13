package com.capstone.javabox.csit228.games.wordle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WordRetriever {
    private final int numWord;
    private String targetWord = "APPLE";
    private List<String> wordList = new ArrayList<>();

    public WordRetriever(int numWord) {
        this.numWord = numWord;
        this.loadAndGenerate();
    }

    private void loadAndGenerate() {
        this.setWordList();
        if (!wordList.isEmpty()) {
            // Pick target word using the same index logic
            this.targetWord = wordList.get(Math.abs(numWord % wordList.size()));
        }
    }

    /**
     * Uses FileReader just like Hangman to look at the physical disk path.
     */
    private void setWordList() {
        String path = "src/main/java/com/capstone/javabox/csit228/games/5-letter-words.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim().toUpperCase();
                if (!trimmed.isEmpty()) {
                    this.wordList.add(trimmed);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading word list: " + e.getMessage());
        }
    }

    /**
     * Multithreaded Search (Lesson requirement)
     */
    public boolean isValidWord(String guess) {
        if (guess == null || wordList.isEmpty()) return false;

        String upperGuess = guess.trim().toUpperCase();
        int numThreads = 4;
        Thread[] threads = new Thread[numThreads];
        AtomicBoolean found = new AtomicBoolean(false);

        int totalSize = wordList.size();
        int chunkSize = totalSize / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * chunkSize;
            int end = (i == numThreads - 1) ? totalSize : (start + chunkSize);

            threads[i] = new Thread(new WordSearchTask(start, end, upperGuess, found));
            threads[i].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return found.get();
    }

    private class WordSearchTask implements Runnable {
        private final int start, end;
        private final String target;
        private final AtomicBoolean found;

        public WordSearchTask(int start, int end, String target, AtomicBoolean found) {
            this.start = start;
            this.end = end;
            this.target = target;
            this.found = found;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                if (found.get()) return; // Early exit if another thread found it
                if (wordList.get(i).equals(target)) {
                    found.set(true);
                    return;
                }
            }
        }
    }

    public String getWord() { return targetWord; }
}