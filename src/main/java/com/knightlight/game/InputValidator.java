package com.knightlight.game;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe utility for validating inputs across the roguelike system.
 * Provides static methods for common validation patterns used by dungeon,
 * combat, player, and equipment systems.
 */
public class InputValidator {
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Private constructor prevents instantiation
    private InputValidator() {
        throw new AssertionError("InputValidator is a utility class");
    }

    /**
     * Validate floor number for dungeon progression.
     * @param floorNumber the floor to validate
     * @param maxFloor the maximum allowed floor
     * @return true if valid (1 <= floorNumber <= maxFloor), false otherwise
     */
    public static boolean isValidFloorNumber(int floorNumber, int maxFloor) {
        return floorNumber >= 1 && floorNumber <= maxFloor;
    }

    /**
     * Validate base dungeon floor number (1-99).
     * @param floorNumber the floor to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidFloorNumber(int floorNumber) {
        return isValidFloorNumber(floorNumber, 99);
    }

    /**
     * Validate enemy count for a dungeon level.
     * @param baseEnemyCount the count to validate
     * @return true if valid (1-20), false otherwise
     */
    public static boolean isValidBaseEnemyCount(int baseEnemyCount) {
        return baseEnemyCount >= 1 && baseEnemyCount <= 20;
    }

    /**
     * Validate difficulty scaling multiplier.
     * @param difficultyScaling the multiplier to validate
     * @return true if valid (0.5-3.0), false otherwise
     */
    public static boolean isValidDifficultyScaling(double difficultyScaling) {
        return difficultyScaling >= 0.5 && difficultyScaling <= 3.0;
    }

    /**
     * Validate rarity threshold for loot drops.
     * @param rarityThreshold the threshold to validate
     * @return true if valid (0.0-1.0), false otherwise
     */
    public static boolean isValidRarityThreshold(double rarityThreshold) {
        return rarityThreshold >= 0.0 && rarityThreshold <= 1.0;
    }

    /**
     * Validate a player or character name.
     * @param name the name to validate
     * @return true if valid (1-32 characters, alphanumeric + space/underscore), false otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 32) {
            return false;
        }
        return name.matches("[a-zA-Z0-9_\\s]+");
    }

    /**
     * Validate a character's health value.
     * @param health the health to validate
     * @return true if valid (>= 0), false otherwise
     */
    public static boolean isValidHealth(int health) {
        return health >= 0;
    }

    /**
     * Validate a character's attack stat.
     * @param attack the attack value to validate
     * @return true if valid (>= 1), false otherwise
     */
    public static boolean isValidAttack(int attack) {
        return attack >= 1;
    }

    /**
     * Validate a character's defense stat.
     * @param defense the defense value to validate
     * @return true if valid (>= 0), false otherwise
     */
    public static boolean isValidDefense(int defense) {
        return defense >= 0;
    }

    /**
     * Validate a player level.
     * @param level the level to validate
     * @return true if valid (1-100), false otherwise
     */
    public static boolean isValidPlayerLevel(int level) {
        return level >= 1 && level <= 100;
    }

    /**
     * Validate experience points.
     * @param experience the XP to validate
     * @return true if valid (>= 0), false otherwise
     */
    public static boolean isValidExperience(int experience) {
        return experience >= 0;
    }

    /**
     * Validate inventory capacity.
     * @param capacity the capacity to validate
     * @return true if valid (1-100), false otherwise
     */
    public static boolean isValidInventoryCapacity(int capacity) {
        return capacity >= 1 && capacity <= 100;
    }

    /**
     * Validate current inventory size.
     * @param size the size to validate
     * @param capacity the max capacity
     * @return true if valid (0 <= size <= capacity), false otherwise
     */
    public static boolean isValidInventorySize(int size, int capacity) {
        return size >= 0 && size <= capacity;
    }

    /**
     * Validate an equipment stat bonus (ATK, DEF, or HP bonus).
     * @param bonus the bonus to validate
     * @return true if valid (-50 to +100), false otherwise
     */
    public static boolean isValidEquipmentBonus(int bonus) {
        return bonus >= -50 && bonus <= 100;
    }

    /**
     * Validate a hit chance probability.
     * @param hitChance the probability to validate
     * @return true if valid (0.0-1.0), false otherwise
     */
    public static boolean isValidHitChance(double hitChance) {
        return hitChance >= 0.0 && hitChance <= 1.0;
    }

    /**
     * Validate damage amount.
     * @param damage the damage to validate
     * @return true if valid (>= 1), false otherwise
     */
    public static boolean isValidDamage(int damage) {
        return damage >= 1;
    }

    /**
     * Validate enemy level (scaled with floor).
     * @param enemyLevel the level to validate
     * @return true if valid (1-100), false otherwise
     */
    public static boolean isValidEnemyLevel(int enemyLevel) {
        return enemyLevel >= 1 && enemyLevel <= 100;
    }

    /**
     * Validate experience reward amount.
     * @param reward the reward to validate
     * @return true if valid (>= 10), false otherwise
     */
    public static boolean isValidExperienceReward(int reward) {
        return reward >= 10;
    }

    /**
     * Validate a percentage value.
     * @param percentage the percentage to validate
     * @return true if valid (0-100), false otherwise
     */
    public static boolean isValidPercentage(double percentage) {
        return percentage >= 0.0 && percentage <= 100.0;
    }

    /**
     * Validate a multiplier/scalar value (commonly used for floor scaling).
     * @param scalar the scalar to validate
     * @return true if valid (0.1-10.0), false otherwise
     */
    public static boolean isValidScalar(double scalar) {
        return scalar >= 0.1 && scalar <= 10.0;
    }

    /**
     * Validate a non-null object.
     * @param obj the object to validate
     * @return true if not null, false otherwise
     */
    public static boolean isNotNull(Object obj) {
        return obj != null;
    }

    /**
     * Validate a non-empty string.
     * @param str the string to validate
     * @return true if not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * Clamp an integer value to a range.
     * @param value the value to clamp
     * @param min the minimum value
     * @param max the maximum value
     * @return clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Clamp a double value to a range.
     * @param value the value to clamp
     * @param min the minimum value
     * @param max the maximum value
     * @return clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public String toString() {
        return "InputValidator{utility class}";
    }
}
