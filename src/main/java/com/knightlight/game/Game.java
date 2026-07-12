package com.knightlight.game;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private static final String TAG = "Game";
    
    // Game constants
    private static final int ENEMY_HEALTH = 10;
    private static final int ENEMY_ATTACK_POWER = 5;
    private static final int GOLD_PICKUP_AMOUNT = 10;
    private static final int GOLD_WIN_CONDITION = 100;
    private static final int SPAWN_ATTEMPTS = 100;
    
    public enum GameState {
        PLAYING, WON, LOST
    }

    private final Object stateLock = new Object();
    private final Object enemyLock = new Object();
    
    private Dungeon dungeon;
    private Player player;
    private List<Enemy> enemies;
    private GameState state;
    private int turnCount;
    private int initialEnemyCount;
    private Random rng;

    public Game(int dungeonWidth, int dungeonHeight, int numEnemies) {
        if (dungeonWidth <= 0 || dungeonHeight <= 0 || numEnemies < 0) {
            throw new IllegalArgumentException(
                "Dungeon width/height must be > 0; numEnemies must be >= 0");
        }

        this.dungeon = new Dungeon(dungeonWidth, dungeonHeight);
        if (dungeon == null) {
            throw new IllegalStateException("Failed to create Dungeon");
        }

        this.enemies = new ArrayList<>();
        this.player = new Player(2, 2);
        if (player == null) {
            throw new IllegalStateException("Failed to create Player");
        }

        this.state = GameState.PLAYING;
        this.turnCount = 0;
        this.initialEnemyCount = numEnemies;
        this.rng = new Random(System.nanoTime());

        // Spawn enemies
        for (int i = 0; i < numEnemies; i++) {
            Enemy enemy = spawnEnemy();
            if (enemy != null) {
                synchronized (enemyLock) {
                    enemies.add(enemy);
                }
                Log.d(TAG, "Spawned enemy " + i + " at (" + enemy.getX() + ", " + enemy.getY() + ")");
            } else {
                Log.w(TAG, "Failed to spawn enemy " + i + " after " + SPAWN_ATTEMPTS + " attempts");
            }
        }

        Log.d(TAG, "Game initialized: " + enemies.size() + "/" + numEnemies 
            + " enemies spawned, dungeon: " + dungeonWidth + "x" + dungeonHeight);
    }

    /**
     * Spawn a single enemy at a random walkable location.
     * Not player's starting position (2,2).
     */
    private Enemy spawnEnemy() {
        if (dungeon == null) {
            Log.e(TAG, "Dungeon is null; cannot spawn enemy");
            return null;
        }

        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int x = rng.nextInt(dungeon.getWidth());
            int y = rng.nextInt(dungeon.getHeight());

            // Validate bounds and walkability
            if (!dungeon.isInBounds(x, y)) {
                continue;
            }

            if (dungeon.isWalkable(x, y) && !(x == player.getX() && y == player.getY())) {
                return new Enemy(x, y, ENEMY_HEALTH, ENEMY_ATTACK_POWER, dungeon);
            }
        }

        Log.w(TAG, "spawnEnemy() exhausted " + SPAWN_ATTEMPTS + " attempts");
        return null;
    }

    /**
     * Move player in direction (dx, dy) and process one turn.
     * Synchronized to prevent race conditions with processTurn().
     */
    public synchronized void playerMove(int dx, int dy) {
        synchronized (stateLock) {
            if (state != GameState.PLAYING) {
                Log.w(TAG, "playerMove called but game state is " + state);
                return;
            }
        }

        // Validate movement direction
        if ((dx < -1 || dx > 1) || (dy < -1 || dy > 1) || (dx == 0 && dy == 0)) {
            Log.w(TAG, "Invalid move direction: dx=" + dx + ", dy=" + dy);
            return;
        }

        if (player == null) {
            Log.e(TAG, "Player is null in playerMove");
            return;
        }

        player.move(dx, dy);
        if (player.getX() < 0 || player.getY() < 0 || player.getX() >= dungeon.getWidth() || player.getY() >= dungeon.getHeight() || dungeon.getTile(player.getX(), player.getY()) == Dungeon.TILE_WALL) {
            player.move(-dx, -dy);
            return;
        }
        processTurn();
    }

    /**
     * Process one turn: move enemies, resolve combat, pickup gold, check win/loss.
     * Called only from playerMove() which is synchronized.
     */
    private void processTurn() {
        if (player == null) {
            Log.e(TAG, "Player is null in processTurn");
            return;
        }

        turnCount++;

        // Step 1: Move all alive enemies toward player
        synchronized (enemyLock) {
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive()) {
                    enemy.moveToward(player);
                }
            }
        }

        // Step 2: Resolve combat: check all enemies at player location
        synchronized (enemyLock) {
            for (Enemy enemy : enemies) {
                if (enemy != null && enemy.isAlive() 
                    && enemy.getX() == player.getX() && enemy.getY() == player.getY()) {
                    
                    player.takeDamage(enemy.getAttack());
                    enemy.takeDamage(5);
                    Log.d(TAG, "Combat: Player takes " + enemy.getAttack() 
                        + " damage, enemy takes 5 damage");
                }
            }
        }

        // Step 3: Pickup gold if on gold tile
        if (dungeon != null) {
            int playerX = player.getX();
            int playerY = player.getY();
            
            if (dungeon.isInBounds(playerX, playerY) 
                && dungeon.getTile(playerX, playerY) == Dungeon.TILE_GOLD) {
                
                player.addGold(GOLD_PICKUP_AMOUNT);
                dungeon.setTile(playerX, playerY, Dungeon.TILE_FLOOR);
                Log.d(TAG, "Player picked up gold. Total: " + player.getGoldCollected());
            }
        }

        // Step 4: Check win/loss conditions
        synchronized (stateLock) {
            if (!player.isAlive()) {
                state = GameState.LOST;
                Log.d(TAG, "Player defeated. Game Over.");
            } else if (player.getGoldCollected() >= GOLD_WIN_CONDITION) {
                state = GameState.WON;
                Log.d(TAG, "Player collected " + GOLD_WIN_CONDITION 
                    + " gold. Victory!");
            }
        }
    }

    /**
     * Get a defensive copy of the dungeon.
     */
    public synchronized Dungeon getDungeon() {
        return dungeon;
    }

    /**
     * Get a defensive copy of the player.
     */
    public synchronized Player getPlayer() {
        return player;
    }

    /**
     * Get a defensive copy of the enemies list.
     */
    public synchronized List<Enemy> getEnemies() {
        synchronized (enemyLock) {
            return new ArrayList<>(enemies);
        }
    }

    /**
     * Get current game state.
     */
    public synchronized GameState getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    /**
     * Get turn count.
     */
    public synchronized int getTurnCount() {
        return turnCount;
    }

    /**
     * Reset game to initial state with same dungeon dimensions and initial enemy count.
     */
    public synchronized void reset() {
        if (dungeon == null) {
            Log.e(TAG, "Dungeon is null; cannot reset");
            return;
        }

        synchronized (stateLock) {
            synchronized (enemyLock) {
                dungeon = new Dungeon(dungeon.getWidth(), dungeon.getHeight());
                enemies.clear();
                player = new Player(2, 2);
                state = GameState.PLAYING;
                turnCount = 0;
                rng = new Random(System.nanoTime());

                // Respawn initial enemies
                for (int i = 0; i < initialEnemyCount; i++) {
                    Enemy enemy = spawnEnemy();
                    if (enemy != null) {
                        enemies.add(enemy);
                        Log.d(TAG, "Reset: Spawned enemy " + i);
                    }
                }

                Log.d(TAG, "Game reset. Enemies: " + enemies.size() + "/" + initialEnemyCount);
            }
        }
    }
}
