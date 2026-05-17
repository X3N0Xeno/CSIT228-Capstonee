package com.capstone.javabox.csit228.games.chainreaction;

import java.util.*;

public class ChainReactionGame {

    public static class ExplosionWave {
        public final List<int[]> explodingCells;
        public ExplosionWave(List<int[]> cells) { this.explodingCells = cells; }
    }

    public static class InfectionResult {
        public final List<int[]> infectedCells;
        public final List<int[]> convertedCells;
        public final List<int[]> diedCells;
        public InfectionResult(List<int[]> infected, List<int[]> converted, List<int[]> died) {
            this.infectedCells = infected;
            this.convertedCells = converted;
            this.diedCells = died;
        }
    }

    private final Cell[][] board;
    private final int rows;
    private final int cols;
    private final int playerCount;
    private final Random random = new Random();

    public ChainReactionGame(int rows, int cols, int playerCount) {
        this.rows = rows;
        this.cols = cols;
        this.playerCount = playerCount;
        this.board = new Cell[rows][cols];

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                board[r][c] = new Cell();

        placeSpecialCells();
    }

    private void placeSpecialCells() {
        int fortified = (rows == 9) ? 3 : 8;
        int multiplier = (rows == 9) ? 2 : 5;
        Set<String> used = new HashSet<>();

        placeRandom(Cell.CellType.FORTIFIED, fortified, used);
        placeRandom(Cell.CellType.MULTIPLIER, multiplier, used);
    }

    private void placeRandom(Cell.CellType type, int count, Set<String> used) {
        int placed = 0;
        while (placed < count) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            String key = r + "," + c;
            if (!used.contains(key)) {
                board[r][c].setType(type);
                used.add(key);
                placed++;
            }
        }
    }

    public List<ExplosionWave> placeOrb(int row, int col, int playerIndex) {
        board[row][col].addOrb(playerIndex);
        return resolveExplosions();
    }

    private List<ExplosionWave> resolveExplosions() {
        List<ExplosionWave> waves = new ArrayList<>();
        boolean anyExploded;

        do {
            anyExploded = false;
            List<int[]> wave = new ArrayList<>();

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Cell cell = board[r][c];
                    if (!cell.isEmpty() && cell.getOrbCount() >= cell.getCriticalMass(r, c, rows, cols)) {
                        wave.add(new int[]{r, c});
                    }
                }
            }

            if (!wave.isEmpty()) {
                anyExploded = true;
                waves.add(new ExplosionWave(wave));

                Map<String, Integer> ownerSnapshot = new HashMap<>();
                for (int[] cell : wave) {
                    ownerSnapshot.put(cell[0] + "," + cell[1], board[cell[0]][cell[1]].getOwner());
                }

                for (int[] pos : wave) {
                    int r = pos[0], c = pos[1];
                    int owner = ownerSnapshot.get(r + "," + c);
                    int orbsToSend = board[r][c].getType() == Cell.CellType.MULTIPLIER ? 2 : 1;

                    board[r][c].clear();

                    for (int[] neighbor : getNeighbors(r, c)) {
                        int nr = neighbor[0], nc = neighbor[1];
                        for (int i = 0; i < orbsToSend; i++) {
                            board[nr][nc].addOrb(owner);
                        }
                    }
                }
            }

        } while (anyExploded);

        return waves;
    }

    // Only infects cells that were part of the chain reaction
    public InfectionResult runInfectionPhase(List<ExplosionWave> waves) {
        List<int[]> infectedCells  = new ArrayList<>();
        List<int[]> convertedCells = new ArrayList<>();
        List<int[]> diedCells      = new ArrayList<>();

        // Build set of chain-involved cells (exploding cells + their neighbors)
        Set<String> chainCells = new LinkedHashSet<>();
        for (ExplosionWave wave : waves) {
            for (int[] pos : wave.explodingCells) {
                chainCells.add(pos[0] + "," + pos[1]);
                for (int[] neighbor : getNeighbors(pos[0], pos[1])) {
                    chainCells.add(neighbor[0] + "," + neighbor[1]);
                }
            }
        }

        // Snapshot owners to avoid cascading mid-infection
        int[][] ownerSnapshot = new int[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                ownerSnapshot[r][c] = board[r][c].getOwner();

        // Only iterate chain-involved cells
        for (String key : chainCells) {
            String[] parts = key.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);

            if (board[r][c].isEmpty()) continue;
            if (random.nextInt(100) >= 25) continue; // 25% chance to infect

            infectedCells.add(new int[]{r, c});
            int infectorOwner = ownerSnapshot[r][c];

            for (int[] neighbor : getNeighbors(r, c)) {
                int nr = neighbor[0], nc = neighbor[1];
                int neighborOwner = ownerSnapshot[nr][nc];

                if (neighborOwner == -1 || neighborOwner == infectorOwner) continue;

                // 50/50 convert or die
                if (random.nextBoolean()) {
                    board[nr][nc].convertTo(infectorOwner);
                    convertedCells.add(new int[]{nr, nc});
                } else {
                    board[nr][nc].removeOrb();
                    diedCells.add(new int[]{nr, nc});
                }
            }
        }

        return new InfectionResult(infectedCells, convertedCells, diedCells);
    }

    public List<int[]> getNeighbors(int r, int c) {
        List<int[]> neighbors = new ArrayList<>();
        if (r > 0)        neighbors.add(new int[]{r - 1, c});
        if (r < rows - 1) neighbors.add(new int[]{r + 1, c});
        if (c > 0)        neighbors.add(new int[]{r, c - 1});
        if (c < cols - 1) neighbors.add(new int[]{r, c + 1});
        return neighbors;
    }

    public boolean isEliminated(int playerIndex, boolean gameStarted) {
        if (!gameStarted) return false;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (board[r][c].getOwner() == playerIndex) return false;
        return true;
    }

    public int countSurvivors(boolean[] eliminated) {
        Set<Integer> alive = new HashSet<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (board[r][c].getOwner() >= 0)
                    alive.add(board[r][c].getOwner());
        int count = 0;
        for (int owner : alive)
            if (!eliminated[owner]) count++;
        return count;
    }

    public int getWinner(boolean[] eliminated) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                int owner = board[r][c].getOwner();
                if (owner >= 0 && !eliminated[owner]) return owner;
            }
        return -1;
    }

    public Cell[][] getBoard() { return board; }
    public int getRows()       { return rows; }
    public int getCols()       { return cols; }
}