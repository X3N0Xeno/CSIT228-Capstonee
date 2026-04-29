package com.example.hangman;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class WordRetriever {
    int numWord;
    private String word;
    public WordRetriever(int numWord) {
        this.numWord = numWord;
        generateWord();
    }
    public void generateWord() {
        try(BufferedReader br = new BufferedReader(new FileReader("5-letter-words.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                if(i == numWord){
                    word = line;
                    break;
                }
                i++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return word;
    }
}
