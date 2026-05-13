package com.capstone.javabox.csit228.games.hangman;
import java.util.*;
public class Hangman {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int numWord;
        numWord = (int) (Math.random() * 3103);
        WordRetriever wordRetriever = new WordRetriever(numWord);

        String word = wordRetriever.getWord();
        char[] letters = new char[word.length()];
        Arrays.fill(letters, '_');
        int wrongGuess = 0;
        while(true){
            System.out.print("Gimme Letter: ");
            char ch = sc.next().charAt(0);
            sc.nextLine();
            boolean guess = false;

            for(int i = 0; i < word.length(); i++){
                if(word.charAt(i) == ch){
                    letters[i] = ch;
                    guess = true;
                }
            }

            if(!guess){
                wrongGuess++;
                System.out.println("Wrong Guess! " + wrongGuess + " Bullet/s now");

                int roulette = (int)(Math.random()*7) + 1;
                if(wrongGuess >= roulette){
                    System.out.println("You dead after " +wrongGuess + " Bullets");
                    break;
                }else {
                    System.out.println(" *click* ... You survived. Probability of death next time: " + (wrongGuess + 1) + "/7");
                }
            }else{
                for(char letter : letters){
                    System.out.print(letter);
                }
                System.out.println();
            }


            String currWord = new String(letters);
            if(currWord.equals(word)){
                System.out.println("Yey you nut dumb");
                break;
            }
        }
        System.out.println("Word is: " + word);
    }

}
