package com.capstone.javabox.csit228.games.wordle;

public class WordleGame {
    private final String secretWord;
    private final boolean win;
    private final int guessesUsed;
    private final String[] guesses;
    private final int[] correctCounts;
    private final int[] misplacedCounts;

    public WordleGame(String secretWord, boolean win, int guessesUsed,
                      String[] guesses, int[] correctCounts, int[] misplacedCounts) {
        this.secretWord = secretWord;
        this.win = win;
        this.guessesUsed = guessesUsed;
        this.guesses = guesses;
        this.correctCounts = correctCounts;
        this.misplacedCounts = misplacedCounts;
    }

    public String getSecretWord()       { return secretWord; }
    public boolean isWin()              { return win; }
    public int getGuessesUsed()         { return guessesUsed; }
    public String[] getGuesses()        { return guesses; }
    public int[] getCorrectCounts()     { return correctCounts; }
    public int[] getMisplacedCounts()   { return misplacedCounts; }
}
