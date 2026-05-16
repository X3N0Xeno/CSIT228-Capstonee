package com.capstone.javabox.csit228.games.fullhouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class House {
    public final String name;
    public final String mantra;
    public final String iconPath;   // For the Betting Parlor
    public final String spritePath; // For the Race Track
    public final String propertyValue;
    public final String colorHex;
    public final String specialQuote;

    private double agility;
    private final double RECOVERY_RATE = 15.0;
    private int luck;

    private final double MAX_ENERGY = 100.0;
    private double currentEnergy;

    private double position = 0;
    private double lastPosition = 0;
    private boolean isExhausted = false;
    private int specialMoveTicks = 0;
    private String currentStatus = "Ready at the starting line.";

    private final Random dice = new Random();
    private List<String> bettors = new ArrayList<>();

    public House(String name, String mantra, String iconFileName, String spriteFileName, double agility, String propertyValue, int luck, String colorHex, String specialQuote) {
        this.name = name;
        this.mantra = mantra;
        this.iconPath = "/com/capstone/javabox/csit228/images/" + iconFileName;
        this.spritePath = "/com/capstone/javabox/csit228/images/" + spriteFileName;
        this.agility = agility;
        this.propertyValue = propertyValue;
        this.luck = luck;
        this.colorHex = colorHex;
        this.specialQuote = specialQuote;
        this.currentEnergy = MAX_ENERGY;
    }

    public void addBettor(String playerName) { bettors.add(playerName); }
    public List<String> getBettors() { return bettors; }
    public String getStatus() { return currentStatus; }
    public boolean isSpecialActive() { return specialMoveTicks > 0; }

    public String getStatsString() {
        double maxMult = ((luck - 1) / 2.0) + 1.0;
        return String.format("Agility: %.0f\nValue: %s\nMax Spd: %.1fx", agility, propertyValue, maxMult);
    }

    private double getMultiplier(int roll) {
        switch (roll) {
            case 1: return 0.2;
            case 2: return 0.6;
            case 3: return 1.2;
            case 4: return 2.2;
            case 5: return 3.2;
            case 6: return 4.5;
            default: return 1.0;
        }
    }

    public void takeTurn() {
        lastPosition = position;

        if (specialMoveTicks == 0 && !isExhausted) {
            int secretRoll = dice.nextInt(100) + 1;
            if (secretRoll == 100) {
                specialMoveTicks = 5;
            }
        }

        if (specialMoveTicks > 0) {
            specialMoveTicks--;
            this.position += (agility * 5.0);
            this.currentEnergy = MAX_ENERGY;
            this.currentStatus = "SPECIAL: " + specialQuote;
            return;
        }

        int roll = dice.nextInt(luck) + 1;
        double multiplier = getMultiplier(roll);
        double intendedSpeed = agility * multiplier;

        if (isExhausted) {
            this.position += (intendedSpeed * 0.10);
            this.currentEnergy += RECOVERY_RATE;

            if (multiplier >= 2.2) {
                currentStatus = "*Gasping... pushing!*";
            } else if (multiplier <= 0.6) {
                currentStatus = "*Can't... breathe...*";
            } else {
                currentStatus = "*Crawling forward*";
            }

            if (currentEnergy >= MAX_ENERGY) {
                currentEnergy = MAX_ENERGY;
                isExhausted = false;
                currentStatus = "Second wind!";
            }
        } else {
            this.position += intendedSpeed;
            double energyCost = intendedSpeed * 1.5;
            this.currentEnergy -= energyCost;

            if (multiplier >= 3.2) {
                currentStatus = "\"Massive bound!\"";
            } else if (multiplier >= 1.2) {
                currentStatus = "*Picking up speed*";
            } else {
                currentStatus = "\"Mud on the tracks!\"";
            }

            if (currentEnergy <= 0) {
                isExhausted = true;
                currentEnergy = 0;
                currentStatus = "*Collapsed... burning!*";
            }
        }
    }

    public void resetForNextRace() {
        this.position = 0;
        this.lastPosition = 0;
        this.currentEnergy = MAX_ENERGY;
        this.isExhausted = false;
        this.specialMoveTicks = 0;
        this.currentStatus = "Ready at the starting line.";
        this.bettors.clear();
    }

    public double getPosition() { return position; }
    public double getLastPosition() { return lastPosition; }
    public double getEnergyPercent() { return currentEnergy / MAX_ENERGY; }
    public boolean isExhausted() { return isExhausted; }
}