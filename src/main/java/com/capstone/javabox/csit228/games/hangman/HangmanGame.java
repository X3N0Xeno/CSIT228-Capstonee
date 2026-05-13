package com.capstone.javabox.csit228.games.hangman;

public class HangmanGame {
    private final String word;
    private final boolean win;
    private final int pullsSurvived;

    public HangmanGame(String word, boolean win, int pullsSurvived) {
        this.word = word;
        this.win = win;
        this.pullsSurvived = pullsSurvived;
    }

    public String getWord()         { return word; }
    public boolean isWin()          { return win; }
    public int getPullsSurvived()   { return pullsSurvived; }
}
