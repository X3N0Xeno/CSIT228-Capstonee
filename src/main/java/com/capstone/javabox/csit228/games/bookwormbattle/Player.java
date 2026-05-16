package com.capstone.javabox.csit228.games.bookwormbattle;

import javafx.scene.layout.GridPane;
import java.util.*;

public class Player {
    public String name;
    public double hearts = 20.0;
    public int scrambles = 2;
    public int poisonTicks = 0;
    public boolean hasStrength, isFrozen, isPoweredDown, hasShield;

    public GridPane gridPane;
    public Tile[][] board = new Tile[4][4];
    public List<Tile> selectedTiles = new ArrayList<>();
    public List<GameConstants.CardType> cards = new ArrayList<>();
    public Map<GameConstants.PotionType, Integer> potions = new EnumMap<>(GameConstants.PotionType.class);

    public Player(String n, GridPane gp) {
        this.name = n; this.gridPane = gp;
        for (GameConstants.PotionType pt : GameConstants.PotionType.values()) potions.put(pt, 0);
    }

//    public void takeDamage(double d) {
//        if (hasShield) { hasShield = false; return; }
//        hearts -= (isPoweredDown ? d / 2 : d);
//        isPoweredDown = false;
//    }

    public void takeDamage(double d) {
        if (hasShield) {
            hasShield = false;
            return;
        }
        hearts -= d;
    }

    public void heal(double d) { hearts = Math.min(20, hearts + d); }

    public void addCard(GameConstants.CardType ct) {
        if (cards.size() >= 3) cards.remove(0);
        cards.add(ct);
    }

    public String getSelectedWordString() {
        StringBuilder sb = new StringBuilder();
        for (Tile t : selectedTiles) sb.append(t.letter);
        return sb.toString();
    }

    public void addPotion(GameConstants.PotionType pt) {
        int currentQty = potions.getOrDefault(pt, 0);
        potions.put(pt, currentQty + 1);
    }

    public void refreshBoard() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (board[r][c] != null) {
                    board[r][c].updateVisuals();
                }
            }
        }
    }
}