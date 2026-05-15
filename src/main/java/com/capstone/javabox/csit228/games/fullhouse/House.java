package com.capstone.javabox.csit228.games.fullhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class House {
    public final String name;
    public final String mantra;
    public final String imagePath; // Path for your images

    private double agility, stamina, currentStamina;
    private double position = 0;
    private int luck;
    private boolean isExhausted = false;
    private String currentStatus = "Ready at the starting line.";

    private final Random dice = new Random();
    private List<String> bettors = new ArrayList<>();

    public House(String name, String mantra, String imageFileName, double agility, double stamina, int luck) {
        this.name = name;
        this.mantra = mantra;
        this.imagePath = "/com/capstone/javabox/csit228/games/fullhouse/images/" + imageFileName;
        this.agility = agility;
        this.stamina = stamina;
        this.currentStamina = stamina;
        this.luck = luck;
    }

    public void addBettor(String playerName) { bettors.add(playerName); }
    public List<String> getBettors() { return bettors; }
    public String getStatus() { return currentStatus; }

    public String getStatsString() {
        return String.format("Agi: %.0f | Sta: %.0f | Luck: %d", agility, stamina, luck);
    }

    public void takeTurn() {
        if (isExhausted) {
            currentStatus = "*Out of stamina... resting*";
            recover();
            return;
        }

        int roll = dice.nextInt(6) + 1;
        double speedModifier = (roll <= 2 && dice.nextInt(10) < luck) ? 0.7 : roll / 6.0;

        // Smug and Salty Quotes
        if (roll >= 5) {
            currentStatus = "Smug: \"Eat my dust, peasants!\"";
        } else if (roll <= 2 && speedModifier < 0.7) {
            currentStatus = "Salty: \"This track is bloody rigged!\"";
        } else {
            currentStatus = "*Hopping steadily*";
        }

        this.position += (agility * speedModifier);
        this.currentStamina -= (agility * 0.35);

        if (currentStamina <= 0) {
            isExhausted = true;
            currentStamina = 0;
            currentStatus = "*Collapsed... lungs burning*";
        }
    }

    private void recover() {
        this.currentStamina += 10;
        if (currentStamina >= stamina) {
            currentStamina = stamina;
            isExhausted = false;
            currentStatus = "Back in the race!";
        }
    }

    public void resetForNextRace() {
        this.position = 0;
        this.currentStamina = stamina;
        this.isExhausted = false;
        this.currentStatus = "Ready at the starting line.";
        this.bettors.clear();
    }

    public double getPosition() { return position; }
    public double getStaminaPercent() { return currentStamina / stamina; }
    public boolean isExhausted() { return isExhausted; }
}