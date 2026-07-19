package com.knightlight.game;

/**
 * Centralized game balance and configuration constants.
 * Eliminates magic numbers and improves maintainability.
 */
public class GameBalance {
    
    // Prevent instantiation
    private GameBalance() {
        throw new AssertionError("Cannot instantiate GameBalance");
    }

    // Player balance
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_BASE_ATTACK = 15;
    public static final int PLAYER_INITIAL_GOLD = 0;
    public static final int PLAYER_INITIAL_POTIONS = 3;
    public static final int POTION_HEAL_AMOUNT = 25;

    // Enemy balance
    public static final int ENEMY_BASE_HEALTH = 30;
    public static final int ENEMY_BASE_ATTACK = 8;
    public static final int ENEMY_HEALTH_VARIANCE = 10;
    public static final int ENEMY_GOLD_REWARD = 50;

    // Boss balance
    public static final int BOSS_HEALTH_MULTIPLIER = 2;
    public static final float BOSS_ATTACK_MULTIPLIER = 1.5f;
    public static final int BOSS_GOLD_REWARD = 200;

    // Battle balance
    public static final int BATTLE_DAMAGE_VARIANCE_PERCENT = 25;
    public static final float DEFENSE_REDUCTION_MULTIPLIER = 0.5f;
    public static final double FLEE_SUCCESS_RATE = 0.5;

    // Game progression
    public static final int GOLD_WIN_CONDITION = 500;
    public static final int DUNGEON_WALL_PROBABILITY = 20;
    public static final int DUNGEON_ENEMY_SPAWN_RATE = 30;

    // Difficulty scaling
    public static final float EASY_DIFFICULTY_MULTIPLIER = 0.75f;
    public static final float NORMAL_DIFFICULTY_MULTIPLIER = 1.0f;
    public static final float HARD_DIFFICULTY_MULTIPLIER = 1.25f;

    // Logging
    public static final boolean DEBUG_MODE = true; // Set to BuildConfig.DEBUG in production
}
