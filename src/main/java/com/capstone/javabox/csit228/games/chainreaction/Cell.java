package com.capstone.javabox.csit228.games.chainreaction;

public class Cell {

    public enum CellType { NORMAL, FORTIFIED, MULTIPLIER }

    private int orbCount;
    private int owner; // -1 = empty, 0..3 = player index
    private CellType type;

    public Cell() {
        this.orbCount = 0;
        this.owner = -1;
        this.type = CellType.NORMAL;
    }

    public int getOrbCount()        { return orbCount; }
    public int getOwner()           { return owner; }
    public CellType getType()       { return type; }

    public void setType(CellType type) { this.type = type; }

    public void addOrb(int playerIndex) {
        this.owner = playerIndex;
        this.orbCount++;
    }

    public void removeOrb() {
        orbCount = Math.max(0, orbCount - 1);
        if (orbCount == 0) owner = -1;
    }

    public void convertTo(int playerIndex) {
        this.owner = playerIndex;
    }

    public void clear() {
        orbCount = 0;
        owner = -1;
    }

    public boolean isEmpty() { return orbCount == 0; }

    public int getCriticalMass(int row, int col, int rows, int cols) {
        int base = 0;
        if (row > 0) base++;
        if (row < rows - 1) base++;
        if (col > 0) base++;
        if (col < cols - 1) base++;

        return type == CellType.FORTIFIED ? base * 2 : base;
    }
}