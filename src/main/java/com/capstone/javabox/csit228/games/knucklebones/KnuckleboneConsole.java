package com.capstone.javabox.csit228.games.knucklebones;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class KnuckleboneConsole {
    private KBPlayer p1;
    private KBPlayer p2;
    private KBPlayer current;
    private int dice_val;
    private int winner = 0;

    public KnuckleboneConsole(String p1, String p2){
        this.p1 = new KBPlayer(p1);
        this.p2 = new KBPlayer(p2);
        current = this.p1;
    }

    public KnuckleboneConsole(){
        this.p1 = new KBPlayer("Player 1");
        this.p2 = new KBPlayer("Player 2");
        current = this.p1;
    }

    public int rollDice(){
        dice_val = ThreadLocalRandom.current().nextInt(1,7);
        return dice_val;
    }

    public void printBoard(){
        int[][] temp = current.board;
        for(int i=0; i<3; i++){
            for(int j=0; j<3; j++){
                System.out.print(temp[i][j] + " ");
            }
            System.out.println();
        }
    }

    public boolean insertBoard(int col){
        boolean inserted = current.insertBoard(col, dice_val);

        if(inserted){
            KBPlayer opponent;
            if(current.equals(p1)){
                opponent = p2;
            }
            else{
                opponent = p1;
            }
            opponent.receiveAttack(col, dice_val);
        }

        return inserted;
    }

    public void setCurrent(){
        if(current.equals(p1)){
            current = p2;
            return;
        }
        current = p1;
    }

    private boolean isBoardFull(KBPlayer player) {
        for (int col = 0; col < 3; col++) {
            if (player.board[0][col] == 0) {
                return false;
            }
        }
        return true;
    }

    public static void main() {
        Scanner in = new Scanner(System.in);
        String name1, name2;
        System.out.print("Enter Player 1 name: ");
        name1 = in.nextLine();
        System.out.print("Enter Player 2 name: ");
        name2 = in.nextLine();

        KnuckleboneConsole game = new KnuckleboneConsole(name1, name2);
        String next = "";
        int input1;

        do{
            System.out.println("\nCURRENT SCORE: " + game.p1.getScore() + " - " + game.p2.getScore());
            System.out.println("THE DICE ROLLED: " + game.rollDice());
            System.out.println(game.current.name.toUpperCase() + " BOARD");
            game.printBoard();

            System.out.print("Insert column to place number (1-3): ");
            input1 = in.nextInt();

            while(!game.insertBoard(input1 - 1)){
                System.out.println("Can't insert in that column! Try Again!");
                System.out.print("Insert column to place number (1-3): ");
                input1 = in.nextInt();
            }

            System.out.println(game.current.name.toUpperCase() + " UPDATED BOARD");
            game.printBoard();
            System.out.println("\nUPDATED SCORE: " + game.p1.getScore() + " - " + game.p2.getScore());

            if (game.isBoardFull(game.current)) {
                if (game.p1.getScore() > game.p2.getScore()) game.winner = 1;
                else if (game.p2.getScore() > game.p1.getScore()) game.winner = 2;
                else game.winner = 3;
                break;
            }

            System.out.println("Enter anything to continue to next round...");
            in.nextLine();
            next = in.nextLine();
            game.setCurrent();

        } while(game.winner == 0);

        System.out.println("\nGAME OVER!");
        System.out.println("FINAL SCORE: " + game.p1.getScore() + " - " + game.p2.getScore());
        if (game.winner == 3) {
            System.out.println("It's a tie!");
        } else {
            if(game.winner == 1){
                System.out.println("Player " + game.p1.name + " wins!");
            } else if (game.winner == 2) {
                System.out.println("Player " + game.p2.name + " wins!");
            }
        }
    }
}