package com.capstone.javabox.csit228.games.knucklebones;

public class KBPlayer {
    public String name;
    public int[][] board = new int[3][3];
    private int[] colScores = new int[3];
    private int totalScore;

    public KBPlayer(String name) {
        this.name = name;
    }

    public int getScore(){
        return totalScore;
    }

    public boolean insertBoard(int col, int value){
        int targetRow = -1;
        for(int row = 2; row >= 0; row--){
            if(board[row][col] == 0) {
                targetRow = row;
                break;
            }
        }
        if(targetRow == -1) {
            return false;
        }
        board[targetRow][col] = value;
        int newColScore = calculateColScore(col);
        totalScore = totalScore - colScores[col] + newColScore;
        colScores[col] = newColScore;
        return true;
    }

    public boolean receiveAttack(int col, int opponentValue) {
        boolean blockDestroyed = false;

        for (int row = 0; row < 3; row++) {
            if (board[row][col] == opponentValue) {
                board[row][col] = 0;
                blockDestroyed = true;
            }
        }

        if (!blockDestroyed) {
            return false;
        }

        int[] tempCol = new int[3];
        int tempIdx = 0;

        for (int row = 2; row >= 0; row--) {
            if (board[row][col] != 0) {
                tempCol[tempIdx++] = board[row][col];
            }
        }

        for (int row = 2; row >= 0; row--) {
            if (2 - row < tempIdx) {
                board[row][col] = tempCol[2 - row];
            } else {
                board[row][col] = 0;
            }
        }

        int newColScore = calculateColScore(col);
        totalScore = totalScore - colScores[col] + newColScore;
        colScores[col] = newColScore;

        return true;
    }

    private int calculateColScore(int col){
        int[] freakuency = new int[7];
        for(int row = 0; row < 3; row++){
            if(board[row][col] > 0){
                freakuency[board[row][col]]++;
            }
        }

        int score = 0;
        for(int i = 1; i <= 6; i++){
            if(freakuency[i] > 0){
                score += i * (int)Math.pow(freakuency[i], 2);
            }
        }
        return score;
    }
}