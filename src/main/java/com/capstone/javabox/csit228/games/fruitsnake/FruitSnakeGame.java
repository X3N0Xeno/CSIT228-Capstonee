package com.capstone.javabox.csit228.games.fruitsnake;

import java.util.*;

public class FruitSnakeGame {

    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public static final int COLS = 20;
    public static final int ROWS = 20;
    public static final int MAX_SNAKE_LENGTH = (COLS * ROWS) / 2;
    public static final int OBSTACLE_COUNT = 20;
    public static final int INITIAL_SNAKE_LENGTH = 3;
    public static final int LENGTH_INCREASE_PER_ROUND = 2;

    private final Random random = new Random();

    private int fruitX, fruitY;
    private final Deque<int[]> snake = new ArrayDeque<>();
    private final Set<String> obstacleSet = new HashSet<>();
    private final List<int[]> obstacles = new ArrayList<>();

    private Direction snakeDirection = Direction.RIGHT;
    private int round = 0;
    private int currentSnakeLength;
    private boolean gameOver = false;
    private boolean roundWon = false;

    public FruitSnakeGame() {
        newGame();
    }

    public void newGame() {
        round = 0;
        currentSnakeLength = INITIAL_SNAKE_LENGTH;
        gameOver = false;
        roundWon = false;
        obstacleSet.clear();
        obstacles.clear();
        placeObstacles();
        spawnSnake();
        spawnFruit();
    }

    private void placeObstacles() {
        int placed = 0;
        while (placed < OBSTACLE_COUNT) {
            int r = random.nextInt(ROWS);
            int c = random.nextInt(COLS);
            // Keep center area clear for snake spawn
            if (r >= 3 && r <= ROWS - 4 && c >= 3 && c <= COLS - 4) continue;
            String key = r + "," + c;
            if (!obstacleSet.contains(key)) {
                obstacleSet.add(key);
                obstacles.add(new int[]{r, c});
                placed++;
            }
        }
    }

    public void spawnSnake() {
        snake.clear();
        snakeDirection = Direction.RIGHT;
        // Spawn snake in center-left area
        int startRow = ROWS / 2;
        int startCol = COLS / 4;
        for (int i = 0; i < currentSnakeLength; i++) {
            snake.addLast(new int[]{startRow, startCol - i});
        }
    }

    public void spawnFruit() {
        Set<String> occupied = getOccupiedSet();
        int r, c;
        do {
            r = random.nextInt(ROWS);
            c = random.nextInt(COLS);
        } while (occupied.contains(r + "," + c));
        fruitX = c;
        fruitY = r;
    }

    // Move fruit one step in given direction
    // Returns true if move was valid
    public boolean moveFruit(Direction dir) {
        int nx = fruitX, ny = fruitY;
        switch (dir) {
            case UP    -> ny--;
            case DOWN  -> ny++;
            case LEFT  -> nx--;
            case RIGHT -> nx++;
        }

        if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS) return false;
        if (obstacleSet.contains(ny + "," + nx)) { gameOver = true; return false; }
        if (isSnakeCell(nx, ny)) { gameOver = true; return false; }

        fruitX = nx;
        fruitY = ny;
        return true;
    }

    // Move snake one step using given direction
    // Returns true if snake is still alive
    public boolean moveSnake(Direction dir) {
        int[] head = snake.peekFirst();
        int nx = head[1], ny = head[0];

        switch (dir) {
            case UP    -> ny--;
            case DOWN  -> ny++;
            case LEFT  -> nx--;
            case RIGHT -> nx++;
        }

        snakeDirection = dir;

        // Snake hits wall
        if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS) {
            gameOver = true;
            return false;
        }

        // Snake hits obstacle — round won!
        if (obstacleSet.contains(ny + "," + nx)) {
            roundWon = true;
            return false;
        }

        // Snake hits itself
        if (isSnakeCell(nx, ny)) {
            gameOver = true;
            return false;
        }

        // Snake hits fruit
        if (nx == fruitX && ny == fruitY) {
            gameOver = true;
            return false;
        }

        // Move snake
        snake.addFirst(new int[]{ny, nx});
        if (snake.size() > Math.min(currentSnakeLength, MAX_SNAKE_LENGTH)) {
            snake.removeLast();
        }

        return true;
    }

    public void startNewRound() {
        round++;
        currentSnakeLength = Math.min(
                INITIAL_SNAKE_LENGTH + (round * LENGTH_INCREASE_PER_ROUND),
                MAX_SNAKE_LENGTH
        );
        roundWon = false;
        spawnSnake();
        spawnFruit();
    }

    public Direction getSnakeDirection() { return snakeDirection; }

    private boolean isSnakeCell(int x, int y) {
        for (int[] cell : snake) {
            if (cell[0] == y && cell[1] == x) return true;
        }
        return false;
    }

    private Set<String> getOccupiedSet() {
        Set<String> occupied = new HashSet<>(obstacleSet);
        for (int[] cell : snake) occupied.add(cell[0] + "," + cell[1]);
        occupied.add(fruitY + "," + fruitX);
        return occupied;
    }

    public int[]   getFruitPos()    { return new int[]{fruitY, fruitX}; }
    public Deque<int[]> getSnake()  { return snake; }
    public List<int[]> getObstacles() { return obstacles; }
    public Set<String> getObstacleSet() { return obstacleSet; }
    public boolean isGameOver()     { return gameOver; }
    public boolean isRoundWon()     { return roundWon; }
    public int getRound()           { return round; }
    public int getCurrentSnakeLength() { return currentSnakeLength; }
}