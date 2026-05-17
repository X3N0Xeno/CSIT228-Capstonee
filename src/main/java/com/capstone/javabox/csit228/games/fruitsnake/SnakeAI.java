package com.capstone.javabox.csit228.games.fruitsnake;

import java.util.*;

public class SnakeAI {

    // BFS from snake head to fruit
    // Returns the best direction for the snake to move
    public static FruitSnakeGame.Direction getNextMove(
            FruitSnakeGame game,
            int targetX, int targetY) {

        int[] head = game.getSnake().peekFirst();
        int startRow = head[0];
        int startCol = head[1];

        // Build obstacle + snake body set
        Set<String> blocked = new HashSet<>(game.getObstacleSet());
        boolean first = true;
        for (int[] cell : game.getSnake()) {
            if (first) { first = false; continue; } // skip head
            blocked.add(cell[0] + "," + cell[1]);
        }

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        FruitSnakeGame.Direction[] dirEnums = {
                FruitSnakeGame.Direction.UP,
                FruitSnakeGame.Direction.DOWN,
                FruitSnakeGame.Direction.LEFT,
                FruitSnakeGame.Direction.RIGHT
        };

        // BFS
        Queue<int[]> queue = new LinkedList<>();
        Map<String, int[]> parent = new HashMap<>();
        Map<String, Integer> parentDir = new HashMap<>();

        String startKey = startRow + "," + startCol;
        queue.add(new int[]{startRow, startCol});
        parent.put(startKey, null);

        boolean found = false;
        String targetKey = targetY + "," + targetX;

        while (!queue.isEmpty() && !found) {
            int[] cur = queue.poll();
            for (int d = 0; d < 4; d++) {
                int nr = cur[0] + dirs[d][0];
                int nc = cur[1] + dirs[d][1];
                if (nr < 0 || nr >= FruitSnakeGame.ROWS) continue;
                if (nc < 0 || nc >= FruitSnakeGame.COLS) continue;
                String key = nr + "," + nc;
                if (blocked.contains(key)) continue;
                if (parent.containsKey(key)) continue;

                parent.put(key, cur);
                parentDir.put(key, d);
                queue.add(new int[]{nr, nc});

                if (key.equals(targetKey)) { found = true; break; }
            }
        }

        if (!found) {
            // No path — move randomly to avoid deadlock
            return randomSafeMove(startRow, startCol, blocked, game.getSnakeDirection());
        }

        // Trace back to find first step
        String cur = targetKey;
        String prev = startKey;
        while (true) {
            int[] p = parent.get(cur);
            if (p == null) break;
            String pKey = p[0] + "," + p[1];
            if (pKey.equals(prev)) break;
            cur = pKey;
        }

        return dirEnums[parentDir.get(cur)];
    }

    private static FruitSnakeGame.Direction randomSafeMove(
            int row, int col,
            Set<String> blocked,
            FruitSnakeGame.Direction currentDir) {

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        FruitSnakeGame.Direction[] dirEnums = {
                FruitSnakeGame.Direction.UP,
                FruitSnakeGame.Direction.DOWN,
                FruitSnakeGame.Direction.LEFT,
                FruitSnakeGame.Direction.RIGHT
        };

        List<FruitSnakeGame.Direction> safe = new ArrayList<>();
        for (int d = 0; d < 4; d++) {
            int nr = row + dirs[d][0];
            int nc = col + dirs[d][1];
            if (nr < 0 || nr >= FruitSnakeGame.ROWS) continue;
            if (nc < 0 || nc >= FruitSnakeGame.COLS) continue;
            if (blocked.contains(nr + "," + nc)) continue;
            safe.add(dirEnums[d]);
        }

        if (safe.isEmpty()) return currentDir;
        return safe.get(new Random().nextInt(safe.size()));
    }
}